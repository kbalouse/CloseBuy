package fourpointoh.closebuy;

import android.content.Context;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by Kyle on 11/8/2015.
 */
public class GooglePlacesService {
    private static String apiKey;

    public GooglePlacesService(Context context) {
        apiKey = context.getString(R.string.web_places_api_key);
    }

    public ArrayList<Place> getNearbyPlaces(GooglePlacesRequest request) {
        ArrayList<Place> placesList = new ArrayList<>();
        String httpRequest = buildRequestString(request);

        try {
            placesList = new GooglePlacesTask().execute(httpRequest).get();
        } catch (ExecutionException|InterruptedException e) {
            e.printStackTrace();
        }

        return placesList;
    }

    private static String buildRequestString(GooglePlacesRequest request) {
        ArrayList<String> types = request.getTypes();
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + request.getLatitude() + "," + request.getLongitude());
        sb.append("&radius=" + request.getRadius());
        sb.append("&types=");

        for (int i = 0; i < types.size(); i++) {
            sb.append(types.get(i));
            if (i < types.size() - 1)
                sb.append("|");
        }

        sb.append("&key=" + apiKey);
        return sb.toString();
    }


    private class GooglePlacesTask extends AsyncTask<String, Integer, ArrayList<Place>>
    {
        @Override
        protected ArrayList<Place> doInBackground(String... urls)
        {
            ArrayList<Place> placesList = new ArrayList<>();

            try {
                JSONObject JSONData = readJSONFromUrl(urls[0]);
                placesList = parseJSON(JSONData);
            } catch (IOException e) {
                Log.d("Exception", e.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return placesList;
        }

        private JSONObject readJSONFromUrl(String url) throws IOException, JSONException {
            InputStream is = new URL(url).openStream();
            try {
                BufferedReader bufReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                StringBuilder strBuilder = new StringBuilder();
                int currChar;
                while ((currChar = bufReader.read()) != -1)
                    strBuilder.append((char) currChar);
                String jsonText = strBuilder.toString();

                return new JSONObject(jsonText);
            } finally {
                is.close();
            }
        }

        private ArrayList<Place> parseJSON(JSONObject json) {
            ArrayList<Place> places = new ArrayList<>();

            try {
                JSONArray jsonPlaces = json.getJSONArray("results");
                for(int i = 0; i < jsonPlaces.length(); i++)
                    places.add(parsePlaceJSON((JSONObject) jsonPlaces.get(i)));
            } catch (JSONException e) {
                Log.d("JSON Error", e.getMessage());
            }

            return places;
        }

        private Place parsePlaceJSON(JSONObject jPlace) {
            Place place = new Place();

            try {
                if (!jPlace.isNull("place_id")) {
                    place.setPlaceId(jPlace.getString("place_id")); }
                if (!jPlace.isNull("name")) {
                    place.setName(jPlace.getString("name")); }
                if (!jPlace.isNull("vicinity")) {
                    place.setVicinity(jPlace.getString("vicinity")); }

                place.setLatitude(jPlace.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                place.setLongitude(jPlace.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                place.setIsOpen(jPlace.getJSONObject("opening_hours").getBoolean("open_now"));

                JSONArray jTypes = jPlace.getJSONArray("types");
                ArrayList<String> types = new ArrayList<>();
                for (int i = 0; i < jTypes.length(); i++) {
                    String test = jTypes.get(i).toString();
                    types.add(test);
                }
                place.setTypes(types);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return place;
        }


    }

}

