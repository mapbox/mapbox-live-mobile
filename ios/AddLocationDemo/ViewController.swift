//
//  ViewController.swift
//  AddLocationDemo
//
//  Created by Jordan Kiley on 11/28/17.
//  Copyright © 2017 Mapbox. All rights reserved.
//

/*
 
 See https://www.mapbox.com/install/ios/ for information about installing the framework, as well as step-by-step instructions for getting started.
 
 Getting started:
 1. Drag the Mapbox framework into Embedded Binaries. Make sure to "Copy items if needed" is checked.
 
 2. Add the following run script to the Build Phases tab:
 bash "${BUILT_PRODUCTS_DIR}/${FRAMEWORKS_FOLDER_PATH}/Mapbox.framework/strip-frameworks.sh"
 
 3. Add your API access token (https://www.mapbox.com/studio/account/tokens/) to your Info.plist as the value for "MGLMapboxAccessToken".
 
 Note: For information about how to keep your access token private, please see this guide: https://www.mapbox.com/help/ios-private-access-token/
 
 4. Add a NSLocationWhenInUseUsageDescription key to your Info.plist with a description.
 
 5. Import the Mapbox framework to your ViewController.swift file
 */

import Mapbox

// Add the MGLMapViewDelegate protocol to your view controller.
class ViewController: UIViewController, MGLMapViewDelegate {
    
    @IBOutlet weak var addPoints: UISegmentedControl!
    
    // Create a `mapView` property, then setup your map view.
    var mapView : MGLMapView!
    
    var storeAnnotations : [MGLPointAnnotation] = []
    var features = MGLShapeCollectionFeature()
    var popup : UILabel!
    
    // Layer properties.
    var layer : MGLCircleStyleLayer!
    var clusteredLayer : MGLCircleStyleLayer!
    var unclusteredLayer : MGLCircleStyleLayer!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Create a map view and add it to your view controller.
        mapView = MGLMapView(frame: view.bounds)
        mapView.delegate = self
        mapView.autoresizingMask = [.flexibleHeight, .flexibleWidth]
        mapView.setCenter(CLLocationCoordinate2D(latitude: 37.7468, longitude: -122.4425), zoomLevel: 11, animated: false)
        view.insertSubview(mapView, belowSubview: addPoints)
        
        setupSegmentControl()
        
        // Access a GeoJSON file that is hosted in our dataset, then use it to create a shape collection with attributes attached to it. This collection of points will not be visible (yet). We still need to use it create visible points on the map.
        getLocationsGeoJSON()
    }
    
    func getLocationsGeoJSON() {
        if let url = Bundle.main.url(forResource: "hotels", withExtension: "geojson") {
            let data = try! Data(contentsOf: url)
            
            features = try! MGLShape(data: data, encoding: String.Encoding.utf8.rawValue) as! MGLShapeCollectionFeature
        }
    }
    
    func mapView(_ mapView: MGLMapView, didFinishLoading style: MGLStyle) {
        addPoints.isHidden = false
    }
    
    // MARK: Annotation
    /*
     MGLAnnotations are the most straight forward way to add interactive points to a map. There are built-in methods to create callouts to display text, and, since they are native views, you can animate or style them as you would any other view.
     
     Because they are native views, they can be slower when you have a large quantity of annotations.
     */
    
    func addStoreAnnotations()  {
        if features.shapes.count > storeAnnotations.count { // For the demo only.
            storeAnnotations = [] // For the demo only.
            for feature in features.shapes {
                // Create a point annotation, then set the coordinate 
                let annot = MGLPointAnnotation()
                annot.coordinate = feature.coordinate
                annot.title = feature.attribute(forKey: "listing/name") as? String
                storeAnnotations.append(annot)
            }
        }
        mapView.addAnnotations(storeAnnotations)
    }
    
    // Customize the view for the annotation. You can also use either the default pin annotation or use an MGLAnnotationImage.
    func mapView(_ mapView: MGLMapView, viewFor annotation: MGLAnnotation) -> MGLAnnotationView? {
        
        let annotView = MGLAnnotationView(annotation: annotation, reuseIdentifier: "location-annotation")
        annotView.frame = CGRect(x: 0, y: 0, width: 25, height: 25)
        annotView.layer.cornerRadius = 12.5
        annotView.backgroundColor = UIColor(red:0.77, green:0.73, blue:0.92, alpha:1.0)
        annotView.layer.borderColor = UIColor(red:0.35, green:0.25, blue:0.75, alpha:1.0).cgColor
        annotView.layer.borderWidth = 2
        annotView.isEnabled = true
        
        return annotView
    }
    
    func mapView(_ mapView: MGLMapView, annotationCanShowCallout annotation: MGLAnnotation) -> Bool {
        return true
    }
    
    // MARK: Style Layer
    /*
     Style layers are rendered with OpenGL. They are more performant for displaying large quantities of data. There is also an option to cluster them, which allows you to offer less cluttered maps.
     
     You do need to implement your own tap gesture recognizers for style layers.
     */
    func addStoreCircles() {
        
        if let style = mapView.style {
            if layer != nil &&  !style.layers.contains(layer) {
                style.addLayer(layer)
            } else {
                
                let source = MGLShapeSource(identifier: "sf-points", shape: features, options: nil)
                style.addSource(source)
                
                layer = MGLCircleStyleLayer(identifier: "sf-points-layer", source: source)
                layer.circleRadius = MGLStyleValue(rawValue: 10)
                layer.circleColor = MGLStyleValue(rawValue: UIColor(red:0.77, green:0.73, blue:0.92, alpha:1.0))
                layer.circleStrokeColor = MGLStyleValue(rawValue: UIColor(red:0.35, green:0.25, blue:0.75, alpha:1.0))
                layer.circleStrokeWidth = MGLStyleValue(rawValue: 2)
                style.addLayer(layer)
            }
        }
    }
    
    // MARK: Point clustering.
    /*
     Style layers offer built-in clustering.
     
     */
    func addLocationsWithClustering() {
        if let style = mapView.style {
            if clusteredLayer != nil && !style.layers.contains(clusteredLayer) {
                style.addLayer(clusteredLayer)
                style.addLayer(unclusteredLayer)
            } else {
                if let style = mapView.style, let image = UIImage(named: "home-15.png")  {
                    let source = MGLShapeSource(identifier: "sf-points-clustered", shape: features, options: [.clustered: true, .clusterRadius: 75])
                    style.addSource(source)
                    style.setImage(image, forName: "home")
                    
                    // Create a layer to show clusters of locations.
                    clusteredLayer = MGLCircleStyleLayer(identifier: "sf-points-layer-clustered", source: source)
                    let radiusStops : [Int : MGLStyleValue<NSNumber>] = [0 : MGLStyleValue<NSNumber>(rawValue: 20),
                                                                         30 : MGLStyleValue<NSNumber>(rawValue: 40)]
                    clusteredLayer.circleRadius = MGLStyleValue(interpolationMode: .exponential, sourceStops: radiusStops, attributeName: "point_count", options: nil)
                    
                    clusteredLayer.circleColor = MGLStyleValue(rawValue: UIColor(red:0.35, green:0.25, blue:0.75, alpha:1.0))
                    clusteredLayer.predicate = NSPredicate(format: "%K == YES", "cluster")
                    style.addLayer(clusteredLayer)
                    
                    // Create a layer to show unclustered locations.
                    unclusteredLayer = MGLCircleStyleLayer(identifier: "sf-points-layer-unclustered", source: source)
                    unclusteredLayer.circleRadius = MGLStyleValue(rawValue: 10)
                    unclusteredLayer.circleColor = MGLStyleValue(rawValue: UIColor(red:0.77, green:0.73, blue:0.92, alpha:1.0))
                    unclusteredLayer.circleStrokeColor = MGLStyleValue(rawValue: UIColor(red:0.35, green:0.25, blue:0.75, alpha:1.0))
                    unclusteredLayer.circleStrokeWidth = MGLStyleValue(rawValue: 2)
                    unclusteredLayer.predicate = NSPredicate(format: "%K != YES", "cluster")
                    style.addLayer(unclusteredLayer)
                }
            }
        }
    }
}

// MARK: UISegmentControl setup.
extension ViewController {
    
    func setupSegmentControl() {
        addPoints.addTarget(self, action: #selector(tappedSegmentControl), for: .valueChanged)
    }
    
    @objc func tappedSegmentControl() {
        resetMap()
        switch addPoints.selectedSegmentIndex {
        case 0:
            if mapView.visibleAnnotations?.count == nil || (mapView.visibleAnnotations?.count)! < features.shapes.count {
                addStoreAnnotations()
            }
        case 1:
            addStoreCircles()
        case 2:
            addLocationsWithClustering()
        default:
            return
        }
    }
    
    func resetMap () {
        if layer != nil {
            mapView.style?.removeLayer(layer)
        }
        if clusteredLayer != nil {
            mapView.style?.removeLayer(clusteredLayer)
            mapView.style?.removeLayer(unclusteredLayer)
        }
        mapView.removeAnnotations(storeAnnotations)
    }
}
