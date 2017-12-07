package co.mapbox.mobilesdklivewebinar;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URL;

import static com.mapbox.mapboxsdk.style.functions.Function.property;
import static com.mapbox.mapboxsdk.style.functions.Function.zoom;
import static com.mapbox.mapboxsdk.style.functions.stops.Stop.stop;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.exponential;
import static com.mapbox.mapboxsdk.style.functions.stops.Stops.interval;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textFont;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MainActivity extends AppCompatActivity {
  private MapView mapView;

  private static final String GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID";
  private static final String CIRCLE_LAYER_ID = "CIRCLE_LAYER_ID";
  private CircleLayer circleLayer;
  private MapboxMap mapboxMap;
  private GeoJsonSource geoJsonSource;
  boolean hospitalLayerIsLightRed;
  boolean waterLayerIsLightBlue;
  boolean neighbourhoodLayerIsCustomFont;
  boolean parkLayerIsLightGreen;
  private String TAG = "MainActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // TODO: MAKE SURE YOU HAVE PASTED YOUR MAPBOX ACCESS TOKEN IN THE STRINGS FILE!
    Mapbox.getInstance(this, getString(R.string.mapbox_account_access_token));

    setContentView(R.layout.activity_main);

    hospitalLayerIsLightRed = true;
    waterLayerIsLightBlue = true;
    parkLayerIsLightGreen = true;
    neighbourhoodLayerIsCustomFont = false;

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(MapboxMap mapboxMap) {

        MainActivity.this.mapboxMap = mapboxMap;

        setUpGeojsonSource();

        setUpCircles();

      }
    });
  }

  private void setUpGeojsonSource() {

    // Adding circle layer data-driven styling based on external data
    try {
      // Load GeoJSONSource

      // TODO: PASTE IN THE URL POINTING TO THE DATA SET ON THE MAPBOX DATASETS API THAT YOU WANT TO USE!
      geoJsonSource = new GeoJsonSource(GEOJSON_SOURCE_ID,
        new URL("PASTE_IN_YOUR_MAPBOX_DATASETS_API_URL"));

      mapboxMap.addSource(geoJsonSource);

    } catch (Throwable throwable) {
      Log.d("MainActivity", "Couldn't add geoJsonSource to map", throwable);
    }
  }

  private void setUpCircles() {
    circleLayer = new CircleLayer(CIRCLE_LAYER_ID, GEOJSON_SOURCE_ID);
    circleLayer.setProperties(
      circleRadius(
        zoom(
          exponential(
            stop(12, circleRadius(10f)),
            stop(22, circleRadius(6f))
          ).withBase(1.75f)
        )
      ),

      // TODO: CHOOSE A GEOJSON DATA PROPERTY FROM YOUR DATASET FOR BELOW
      circleColor(property("PASTE_IN_KEY_VALUE_OF_GEOJSON_PROPERTY_YOU_WANT_TO_USE", interval(
        stop(0, fillColor(Color.GREEN)),
        stop(80, fillColor(Color.YELLOW)),
        stop(150, fillColor(Color.BLUE))))));

    mapboxMap.addLayer(circleLayer);
    circleLayer.setProperties(visibility("none"));
  }

  private void toggleParkGreen() {
    Layer parkLayer = mapboxMap.getLayer("park");

    if (parkLayer == null) {
      return;
    }

    if (parkLayerIsLightGreen) {
      parkLayer.setProperties(fillColor(Color.GREEN)
      );
      parkLayerIsLightGreen = false;
    } else {
      // Layer isn't visible
      parkLayer.setProperties(
        fillColor("#B6E59E"));
      parkLayerIsLightGreen = true;

    }
  }

  private void toggleNeighbourhoodFont() {

    Layer neighbourhoodLayer = mapboxMap.getLayer("place-neighbourhood");

    if (neighbourhoodLayer == null) {
      return;
    }

    String[] standardFont = new String[] {"DIN Offc Pro Regular"};

    String[] customFont = new String[] {"Clan Offc Pro Extd Bold"};

    // Lato Black

    if (neighbourhoodLayerIsCustomFont) {
      neighbourhoodLayer.setProperties(textFont(standardFont)
      );
      neighbourhoodLayerIsCustomFont = false;
    } else {
      // Layer isn't visible
      neighbourhoodLayer.setProperties(textFont(customFont));
      neighbourhoodLayerIsCustomFont = true;

    }
  }

  private void toggleHospitalRed() {
    Layer hospitalLayer = mapboxMap.getLayer("hospital");

    if (hospitalLayer == null) {
      return;
    }

    if (hospitalLayerIsLightRed) {
      hospitalLayer.setProperties(fillColor("#FF1900")
      );
      hospitalLayerIsLightRed = false;
    } else {
      // Layer isn't visible
      hospitalLayer.setProperties(
        fillColor("#ead2da"));
      hospitalLayerIsLightRed = true;

    }
  }

  private void toggleWaterBlue() {

    Layer waterLayer = mapboxMap.getLayer("water");

    if (waterLayer == null) {
      return;
    }

    if (waterLayerIsLightBlue) {
      waterLayer.setProperties(fillColor(ContextCompat.getColor(this, R.color.colorPrimary)));

      waterLayerIsLightBlue = false;
    } else {
      // Layer isn't visible
      waterLayer.setProperties(
        fillColor("#75cff0"));
      waterLayerIsLightBlue = true;

    }
  }

  private void setLayerVisible(String layerId) {
    Layer specifiedMapLayer = mapboxMap.getLayer(layerId);
    if (specifiedMapLayer == null) {
      return;
    }
    if (VISIBLE.equals(specifiedMapLayer.getVisibility().getValue())) {

      specifiedMapLayer.setProperties(visibility("none"));

    } else {

      specifiedMapLayer.setProperties(visibility("visible"));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case R.id.toggle_circles:

        setLayerVisible(circleLayer.getId());

        return true;

      case R.id.toggle_park_color:

        toggleParkGreen();

        return true;

      case R.id.toggle_hospital_color:

        toggleHospitalRed();

        return true;

      case R.id.toggle_water_color:

        toggleWaterBlue();

        return true;

      case R.id.toggle_large_city_label:

        setLayerVisible("place-city-lg-n");

        return true;

      case R.id.toggle_neighbourhood:

        setLayerVisible("place-neighbourhood");

        return true;

      case R.id.toggle_font:

        toggleNeighbourhoodFont();

        return true;

      case R.id.toggle_school_visibility:

        setLayerVisible("school");

        return true;
    }
    return super.onOptionsItemSelected(item);
  }


  //region lifecycle overrides
  @Override
  public void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }

  //endregion
}