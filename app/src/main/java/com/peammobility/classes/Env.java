package com.peammobility.classes;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class Env extends AppCompatActivity {

    public static final int VOLLEY_TIME_OUT = 120000;
    public static final int RETRIES = 1;
    public static final int COST_PER_VIEW = 2;
    public static final float INTEREST = 10;
    public static final float EXCISE_DUTY = 20;
    public static final float PROCESSING_FEE = 20;
    public static final String APP_VERSION = "1.13";

    //todo ALWAYS CHANGE THIS VALUE TO {{dev}} or {{ live }} WHEN MOVING THE APP TO PLAY STORE
    public static String ENVIRONMENT = "dev";

    //app
    public  static  String PLAYSTORE_APP_URL = "https://play.google.com/store/apps/details?id=world.empower.smart";
    public static String REGISTER_URL = "https://peammobility.com/api/app/register/account";
    public static String LOGIN_URL = "https://peammobility.com/api/app/login";
    public static String AUTH_TOKEN_URL = "https://peammobility.com/api/app/login/token";
    public static String RESET_PIN_URL = "https://peammobility.com/api/app/reset/password";
    public static String UPDATE_PIN_URL = "https://peammobility.com/api/app/update/password";
    public static String UPDATE_IMAGES = "https://peammobility.com/api/app/update/images";
    public static String DEFAULT_PROFILE_IMAGE = "https://peammobility.com/images/profile/user_icon.png";
    public static String GET_PROFILE_IMAGES = "https://peammobility.com/api/app/get/profile/images";
    public static String UPDATE_APP_TOKEN_URL = "https://peammobility.com/api/app/update/app/token";

    //LOANS
    public static String LOAN_CREATE = "https://peammobility.com/api/app/loan/create";

    //CONTACT Web URLs
    public static String FAQ_URL = "https://empowersmart.world/index.php/faq/";
    public static String TERMS_URL = "https://empowersmart.world/index.php/terms_conditions/";
    public static String ABOUT_URL = "https://empowersmart.world/index.php/company-info/";
    public static String WEBSITE_URL = "https://empowersmart.world";
    public static String WHATSAPP_URL = "https://wa.me/254724073319";
    public static String PHONE_URL = "tel://0724073319";
    public static String MAIL_URL = "mailto://support@empowersmart.world";
    public static String LINKED_URL = "https://www.linkedin.com/company/empowersmart/";
    public static String FACEBOOK_URL = "https://www.facebook.com/empower.smart";


    //    public static int FIREBASE_QUERY_RESULTS_LIMIT  = 1000;
    public static int FIREBASE_QUERY_RESULTS_LIMIT = 25;
    public static String APP_BEARER_KEY = "i4wlhgdz*&^%$#@!_-+=?/.><9x1lkdp3ohncb56ka8914c1ucmwq1z*&^%$#@!_-+=?/.><s1mb1l76gnsmhz*&^%$#@!_-+=?/.><tz*&^%$#@!_-+=?/.><97gu9qpbjdfbrld9iy";


    /**
     * To regenerate links according to the environment were in
     * @param link
     * @return
     */
    public static String getURL(String link) {
        StringBuilder sb = new StringBuilder(link);
        sb.delete(0, 8);
        String linking = ENVIRONMENT.equals("dev") ? "https://dev." + sb.toString() : link;
        return  linking;
    }

    public static final String ASSET_FINANCE = "Asset Finance";
    public static final String BUSINESS_LOAN = "Business Loan";
    public static final String PAYROLL_LOAN = "Payroll Loan";
    public static final String PERSONAL_LOAN = "Personal Loan";
    public static final String NORMAL_LOAN = "Normal Loan";
}
