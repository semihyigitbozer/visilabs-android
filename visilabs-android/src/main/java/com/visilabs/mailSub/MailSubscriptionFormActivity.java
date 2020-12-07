package com.visilabs.mailSub;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.visilabs.InAppNotificationState;
import com.visilabs.Visilabs;
import com.visilabs.android.R;
import com.visilabs.api.VisilabsUpdateDisplayState;
import com.visilabs.inApp.FontFamily;
import com.visilabs.inApp.InAppMessage;
import com.visilabs.util.StringUtils;

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailSubscriptionFormActivity extends AppCompatActivity {

    MailSubscriptionForm mailSubscriptionForm;
    ExtendedProps extendedProps;

    private VisilabsUpdateDisplayState mUpdateDisplayState;

    private int mIntentId = -1;

    public static final String INTENT_ID_KEY = "INTENT_ID_KEY";

    LinearLayout llTextContainer;
    ImageButton ibClose;
    TextView tvBody, tvTitle;
    EditText etEmail;
    TextView tvInvalidEmailMessage;
    LinearLayout llEmailPermit;
    CheckBox cbEmailPermit;
    TextView tvEmailPermit;
    LinearLayout llConsent;
    CheckBox cbConsent;
    TextView tvConsent;
    TextView tvCheckConsentMessage;
    Button btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        mIntentId = getIntent().getIntExtra(INTENT_ID_KEY, Integer.MAX_VALUE);
        mailSubscriptionForm = getMailSubscriptionForm();
        try {
            extendedProps = new Gson().fromJson(new java.net.URI(mailSubscriptionForm.getActiondata().getExtendedProps()).getPath(), ExtendedProps.class);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        setContentView( R.layout.activity_mail_subscription_form);
        this.setFinishOnTouchOutside(false);


        llTextContainer = findViewById(R.id.ll_text_container);
        ibClose = findViewById(R.id.ib_close);
        tvBody = findViewById(R.id.tv_body);
        tvTitle = findViewById(R.id.tv_title);
        etEmail = findViewById(R.id.et_email);
        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                    if(checkEmail(etEmail.getText().toString())) {
                        tvInvalidEmailMessage.setVisibility(View.GONE);
                    } else {
                        tvInvalidEmailMessage.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        tvInvalidEmailMessage = findViewById(R.id.tv_invalid_email_message);
        llEmailPermit = findViewById(R.id.ll_email_permit);
        cbEmailPermit = findViewById(R.id.cb_email_permit);
        tvEmailPermit = findViewById(R.id.tv_email_permit);
        tvEmailPermit.setMovementMethod(LinkMovementMethod.getInstance());

        llConsent = findViewById(R.id.ll_consent);
        cbConsent = findViewById(R.id.cb_consent);
        tvConsent = findViewById(R.id.tv_consent);
        tvConsent.setMovementMethod(LinkMovementMethod.getInstance());
        tvCheckConsentMessage = findViewById(R.id.tv_check_consent_message);
        btn = findViewById(R.id.btn);



        if (isShowingInApp() && mailSubscriptionForm != null) {
            setUpView();
        } else {
            VisilabsUpdateDisplayState.releaseDisplayState(mIntentId);
            finish();
        }

        /*


        this.setFinishOnTouchOutside(false);

        ratingBar = findViewById(R.id.ratingBar);
        tvBody = findViewById(R.id.tv_body);
        tvTitle = findViewById(R.id.tv_title);
        btnTemplate = findViewById(R.id.btn_template);
        smileRating = findViewById(R.id.smileRating);
        ivTemplate = findViewById(R.id.iv_template);
        llTextContainer = findViewById(R.id.ll_text_container);
        ibClose = findViewById(R.id.ib_close);


        if (isShowingInApp() && inAppMessage != null) {
            setUpView();
        } else {
            VisilabsUpdateDisplayState.releaseDisplayState(mIntentId);
            finish();
        }

         */
    }

    private void setUpView() {
        llTextContainer.setBackgroundColor(Color.parseColor(extendedProps.getBackground_color()));
        setCloseButton();
        setTitle();
        setBody();
        setEmail();
        setInvalidEmailMessage();
        setCheckBoxes();
        setCheckConsentMessage();
        setButton();
    }

    public void setCloseButton() {
        ibClose.setBackgroundResource(getCloseIcon());
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VisilabsUpdateDisplayState.releaseDisplayState(mIntentId);
                finish();
            }
        });
    }

    private void setTitle() {
        tvTitle.setTypeface(getFont_family(extendedProps.getTitle_font_family()), Typeface.BOLD);
        tvTitle.setText(mailSubscriptionForm.getActiondata().getTitle());
        tvTitle.setTextColor(Color.parseColor(extendedProps.getTitle_text_color()));
        tvTitle.setTextSize(Float.parseFloat(extendedProps.getTitle_text_size()) + 12);
    }

    private void setBody() {
        tvBody.setText(mailSubscriptionForm.getActiondata().getMessage());
        tvBody.setTypeface(getFont_family(extendedProps.getText_font_family()));
        tvBody.setTextColor(Color.parseColor(extendedProps.getText_color()));
        tvBody.setTextSize(Float.parseFloat(extendedProps.getText_size()) + 8);
    }

    private void setEmail() {
        etEmail.setHint(mailSubscriptionForm.getActiondata().getPlaceholder());
    }

    private void setInvalidEmailMessage(){
        tvInvalidEmailMessage.setText(mailSubscriptionForm.getActiondata().getInvalid_email_message());
        tvInvalidEmailMessage.setTextSize(Float.parseFloat(extendedProps.getText_size()) + 8);
        tvInvalidEmailMessage.setTextColor(Color.RED);
    }

    private void setCheckBoxes() {
        if (mailSubscriptionForm.getActiondata().getEmailpermit_text() == null || mailSubscriptionForm.getActiondata().getEmailpermit_text().isEmpty()) {
            llEmailPermit.setVisibility(View.GONE);
        } else {
            tvEmailPermit.setText(createHtml(mailSubscriptionForm.getActiondata().getEmailpermit_text(), extendedProps.getEmailpermit_text_url() ));
            tvEmailPermit.setTextSize(Float.parseFloat(extendedProps.getEmailpermit_text_size()) + 8);
        }
        if (mailSubscriptionForm.getActiondata().getConsent_text() == null || mailSubscriptionForm.getActiondata().getConsent_text().isEmpty()) {
            llConsent.setVisibility(View.GONE);
        } else {
            tvConsent.setText(createHtml(mailSubscriptionForm.getActiondata().getConsent_text(), extendedProps.getConsent_text_url() ));
            tvConsent.setTextSize(Float.parseFloat(extendedProps.getConsent_text_size()) + 8);
        }
    }

    private void setCheckConsentMessage(){
        tvCheckConsentMessage.setText(mailSubscriptionForm.getActiondata().getCheck_consent_message());
        tvCheckConsentMessage.setTextSize(Float.parseFloat(extendedProps.getText_size()) + 8);
        tvCheckConsentMessage.setTextColor(Color.RED);
    }

    private void setButton() {
        btn.setText(mailSubscriptionForm.getActiondata().getButton_label());
        btn.setTypeface(getFont_family(extendedProps.getButton_font_family()));
        btn.setTextColor(Color.parseColor(extendedProps.getButton_text_color()));
        btn.setBackgroundColor(Color.parseColor(extendedProps.getButton_color()));
        btn.setTextSize(Float.parseFloat(extendedProps.getButton_text_size()) + 8);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = etEmail.getText().toString();

                if(checkEmail(email)) {
                    tvInvalidEmailMessage.setVisibility(View.GONE);
                } else {
                    tvInvalidEmailMessage.setVisibility(View.VISIBLE);
                    return;
                }

                if(!checkCheckBoxes()) {
                    return;
                }

                Visilabs.CallAPI().trackMailSubscriptionFormClick(mailSubscriptionForm.getActiondata().getReport());

                //mailSubscriptionForm.getActiondata().getReport().getClick();

                VisilabsUpdateDisplayState.releaseDisplayState(mIntentId);
                finish();
            }
        });
    }

    private Boolean checkCheckBoxes() {
        Boolean isCheckboxesOk = true;

        if(llEmailPermit.getVisibility() != View.GONE){
            if(!cbEmailPermit.isChecked()) {
                isCheckboxesOk = false;
                tvCheckConsentMessage.setVisibility(View.VISIBLE);
                return isCheckboxesOk;
            } else {
                isCheckboxesOk = true;
                tvCheckConsentMessage.setVisibility(View.GONE);
            }
        }
        if(llConsent.getVisibility() != View.GONE){
            if(!cbConsent.isChecked()) {
                isCheckboxesOk = false;
                tvCheckConsentMessage.setVisibility(View.VISIBLE);
                return isCheckboxesOk;
            } else {
                isCheckboxesOk = true;
                tvCheckConsentMessage.setVisibility(View.GONE);
            }
        }

        return isCheckboxesOk;
    }

    private Boolean checkEmail(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Email Permit TextEmail Permit <LINK>TextEmail</LINK> Permit Text
    private Spanned createHtml(String text, String url) {
        if(url == null || url.isEmpty() || !Patterns.WEB_URL.matcher(url).matches()){
            return Html.fromHtml(url.replace("<LINK>", "").replace("</LINK>", ""));
        }
        Pattern pattern = Pattern.compile("<LINK>(\\S+)</LINK>");
        Matcher matcher = pattern.matcher(text);
        Boolean linkMatched = false;
        while (matcher.find()) {
            linkMatched = true;
            String outerHtml = matcher.group(0);
            String innerText = matcher.group(1);
            String s = "<a href=\"" + url + "\">" + innerText +  "</a>";
            text = text.replace(outerHtml, s);
        }
        if(!linkMatched) {
            text = "<a href=\"" + url + "\">" + text +  "</a>";
        }
        return Html.fromHtml(text);
    }

    private Typeface getFont_family(String font_family) {
        if (font_family == null) {
            return Typeface.DEFAULT;
        }
        if (FontFamily.Monospace.toString().equals(font_family.toLowerCase())) {
            return Typeface.MONOSPACE;
        }
        if (FontFamily.SansaSerif.toString().equals(font_family.toLowerCase())) {
            return Typeface.SANS_SERIF;
        }
        if (FontFamily.Serif.toString().equals(font_family.toLowerCase())) {
            return Typeface.SERIF;
        }
        if (FontFamily.Default.toString().equals(font_family.toLowerCase())) {
            return Typeface.DEFAULT;
        }
        return Typeface.DEFAULT;
    }



    private int getCloseIcon() {
        switch (extendedProps.getClose_button_color()) {
            case "white":
                return R.drawable.ic_close_white_24dp;

            case "black":
                return R.drawable.ic_close_black_24dp;
        }
        return R.drawable.ic_close_black_24dp;
    }

    private boolean isShowingInApp() {
        if (mUpdateDisplayState == null) {
            return false;
        }
        return InAppNotificationState.TYPE.equals(mUpdateDisplayState.getDisplayState().getType());
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        VisilabsUpdateDisplayState.releaseDisplayState(mIntentId);
        finish();
    }

    private MailSubscriptionForm getMailSubscriptionForm() {
        InAppNotificationState inAppNotificationState;
        mUpdateDisplayState = VisilabsUpdateDisplayState.claimDisplayState(mIntentId);
        if (mUpdateDisplayState == null || mUpdateDisplayState.getDisplayState() == null) {
            Log.e("Visilabs", "VisilabsNotificationActivity intent received, but nothing was found to show.");
            return null;
        } else {
            inAppNotificationState = (InAppNotificationState) mUpdateDisplayState.getDisplayState();
            return inAppNotificationState.getMailSubscriptionForm();
        }
    }

}
