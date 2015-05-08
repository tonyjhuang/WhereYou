package com.tonyjhuang.whereyou.helpers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by tony on 5/8/15.
 */
public class StreetAddress {

    public static String getStreetAddress(Context context, double lat, double lng, boolean zipCode) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }

                String streetAddress = TextUtils.join(", ", addressFragments);

                // remove zip code
                if(!zipCode) streetAddress = removeZipCode(streetAddress);

                return streetAddress;
            } else {
                return null;
            }
        } catch (IOException e) {
            Log.e("StreetAddress", e.getMessage());
            return null;
        }
    }

    private static String removeZipCode(String stringAddress) {
        String[] arr = stringAddress.split(" ");
        return stringArrayToString(Arrays.copyOf(arr, arr.length - 1));
    }

    private static String stringArrayToString(String[] arr) {
        StringBuilder result = new StringBuilder();
        for (String string : arr) {
            result.append(string);
            result.append(" ");
        }
        return result.toString();
    }

}
