package com.peammobility.resources;

import static android.content.Context.MODE_PRIVATE;

import static com.peammobility.classes.Env.ENVIRONMENT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.peammobility.MainActivity;
import com.peammobility.R;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class CustomResources {
    Activity activity;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    SharedPreferences sharedPreferences;

    public CustomResources(Activity activity) {
        this.activity = activity;
    }

    private FirebaseAuth mAuth;

    public CustomResources() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void setStatusBar() {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, R.color.primary));
    }


    /**
     * date to long
     *
     * @param date
     * @return
     */
    public Long dateToLong(String date) {
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        long milliseconds = 0;
        try {
            Date d = f.parse(date);
            milliseconds = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //  Log.e("milliseconds",Long.toString(milliseconds));
        return milliseconds;
    }


    public String intToMonth(int month_value) {
        String month = null;

        switch (month_value) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return "Wrong Input";
        }
    }


    /**
     * @param jsonString
     * @param data
     * @return
     */
    public String getResponseData(String jsonString, String data) {
        String message = null;
        boolean success = false;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            success = jsonObject.getBoolean("success");
            message = jsonObject.getString("message");
        } catch (Exception e) {
            e.getStackTrace().toString();
        }

        if (data.equals("status")) {
            return success ? "true" : "false";
        }
        return message;
    }


    /**
     * @param jsonString
     * @return
     */
    public boolean extractStatus(String jsonString) {
        boolean success = false;
        Log.e("Response_Extract_login", jsonString);
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            success = jsonObject.getBoolean("success");
        } catch (Exception e) {
            e.getStackTrace().toString();
        }

        return success;
    }


    /**
     * @param jsonString
     * @return
     */
    public String extractUserID(String jsonString) {
        String user_id = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            user_id = jsonObject.getString("userID");
        } catch (Exception e) {
            e.getStackTrace().toString();
        }
        return user_id;
    }


    /**
     * Format Date
     *
     * @param date
     * @return
     */
    public String getDateTime(String date) {
        String[] dmy = date.split(" ");
        String year = dmy[2];

        int month = strToMonth(dmy[0]);
        String day = dmy[1];

        return year + "-" + month + "-" + day;
    }


    public int strToMonth(String month_value) {
        String month = null;

        switch (month_value) {
            case "Jan":
                return 1;
            case "Feb":
                return 2;
            case "Mar":
                return 3;
            case "Apr":
                return 4;
            case "May":
                return 5;
            case "Jun":
                return 6;
            case "Jul":
                return 7;
            case "Aug":
                return 8;
            case "Sep":
                return 9;
            case "Oct":
                return 10;
            case "Nov":
                return 11;
            case "Dec":
                return 12;
            default:
                return 0;
        }
    }

    /**
     * @param jsonString
     * @return
     */
    public String extractData(String jsonString) {
        String data = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            data = jsonObject.getString("data");
        } catch (Exception e) {
            e.getStackTrace().toString();
        }

        return data;
    }


    /**
     * @param jsonString
     * @return
     */
    public String extractData(String jsonString, String key) {
        String data = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            data = jsonObject.getString(key);
        } catch (Exception e) {
            e.getStackTrace().toString();
        }

        return data;
    }


    /**
     * @param jsonString
     * @return
     */
    public String extractUser(String jsonString, String key) {
        String key_value = null;

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject user = new JSONObject(jsonObject.getString("user"));

            key_value = user.getString(key);

        } catch (Exception e) {
            e.getStackTrace().toString();
        }

        return key_value;
    }

    /**
     * @param jsonString
     * @param key
     * @return
     */
    public boolean extractBool(String jsonString, String key) {
        boolean key_value = false;

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject user = new JSONObject(jsonObject.getString("user"));

            key_value = user.getBoolean(key);

        } catch (Exception e) {
            e.getStackTrace().toString();
        }

        return key_value;
    }


    /**
     * @param jsonString
     * @return
     */
    public String extractUserDOB(String jsonString, String key) {
        String key_value = null;

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject user = new JSONObject(jsonObject.getString("user"));
            JSONObject dob = new JSONObject(user.getString("dob"));

            key_value = dob.getString(key);

        } catch (Exception e) {
            e.getStackTrace().toString();
        }

        return key_value;
    }

    /**
     * @param jsonString
     */
    public void saveUserDetails(String jsonString, Activity activity) {
        sharedPreferences = activity.getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("first_name", extractData(jsonString, "firstName"));
        editor.putString("name", extractData(jsonString, "name"));
        editor.putString("phone_number", extractData(jsonString, "phoneNumber"));
        editor.putString("email", extractData(jsonString, "email"));
        editor.putString("createdAT", extractData(jsonString, "createdAT"));
        editor.putBoolean("isActive", extractBool(jsonString, "isActive"));
        editor.putString("pin", extractData(jsonString, "pin"));
        editor.putString("userID", extractData(jsonString, "userID"));

        editor.apply();

    }


    /**
     * @param jsonString
     * @return
     */
    public String extractMessage(String jsonString) {
        String message = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            message = jsonObject.getString("message");
        } catch (Exception e) {
            e.getStackTrace().toString();
        }

        return message;
    }


    /**
     * @param jsonString
     * @return
     */
    public String extractPhoneNumber(String jsonString) {
        String phone_number = null;
        boolean success = false;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            success = jsonObject.getBoolean("success");

            if (success) {
                phone_number = jsonObject.getString("phone_number");
            }
        } catch (Exception e) {
            e.getStackTrace().toString();
        }

        return phone_number;
    }


    /**
     * @param currentActivity
     */
    public void footerData(Activity currentActivity) {
//        CardView homeCard, profileCard, supportCard;
//        ImageView assessorImage;
//        LoadingDialog loadingDialog = new LoadingDialog(currentActivity);
//
//        TextView profileIconText, homeIconText, supportIconText;
//
//        homeCard = currentActivity.findViewById(R.id.footer_home_card);
//        profileCard = currentActivity.findViewById(R.id.footer_profile);
//        supportCard = currentActivity.findViewById(R.id.footer_support);
//
//        //text views
//        profileIconText = currentActivity.findViewById(R.id.icon_profile_text);
//        homeIconText = currentActivity.findViewById(R.id.icon_home_text);
//        supportIconText = currentActivity.findViewById(R.id.icon_support_text);
//
//        profileIconText.setTextColor(currentActivity.getResources().getColor(R.color.primary));
//        homeIconText.setTextColor(currentActivity.getResources().getColor(R.color.primary));
//        supportIconText.setTextColor(currentActivity.getResources().getColor(R.color.primary));
//
//        String activity = currentActivity.getClass().getSimpleName();
//
//        switch (activity) {
//            case "MainActivity":
//                //   homeIconText.setTextColor(currentActivity.getResources().getColor(R.color.secondary));
//                break;
//            case "ProfileActivity":
//                //   profileIconText.setTextColor(currentActivity.getResources().getColor(R.color.secondary));
//                break;
//            case "SupportActivity":
//                // supportIconText.setTextColor(currentActivity.getResources().getColor(R.color.secondary));
//                break;
//        }
//
//        //actions
//        homeCard.setOnClickListener(v -> {
//            homeIconText.setTextColor(currentActivity.getResources().getColor(R.color.secondary));
//            loadingDialog.startLoadingDialog();
//            currentActivity.startActivity(new Intent(currentActivity.getApplicationContext(), MainActivity.class));
//            currentActivity.finish();
//        });
    }


    /**
     * Integer input
     * Number format replica of PHP number format
     *
     * @param number
     * @return
     */

    public String numberFormat(double number) {
        NumberFormat numberFormatInstance = NumberFormat.getInstance(Locale.US);
        return numberFormatInstance.format(number).toString();
    }

    /**
     * String input
     * Number format replica of PHP number format
     *
     * @param number
     * @return
     */

    public String numberFormat(String number) {
        if (number == null) {
            return "0";
        }
        number = number.replace(",", "");
        NumberFormat numberFormatInstance = NumberFormat.getInstance(Locale.US);
        return numberFormatInstance.format(Double.parseDouble(number)).toString();
    }


    /**
     * getCityContry data
     * @param location
     * @return
     */
    public String getCityCountry(String location) {
        String[] split = location.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            if (i == 0) {
                continue;
            }
            sb.append(split[i]);
            if (i != split.length - 1) {
                sb.append(" ");
            }
        }
       return sb.toString();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
