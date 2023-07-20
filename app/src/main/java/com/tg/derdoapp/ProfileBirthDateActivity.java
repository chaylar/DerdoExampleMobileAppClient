package com.tg.derdoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.tg.globals.AppGlobals;
import com.tg.dataObject.UserProfileDataObject;
import com.tg.helper.CustomDialog;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ProfileBirthDateActivity extends BaseActivity {

    EditText date = null;

    public ProfileBirthDateActivity() { super(R.layout.activity_profile_birthdate); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final android.widget.Button profileNameOk = findViewById(R.id.profileNameOK);
        profileNameOk.setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(android.view.View v) {
                GetBirthdateAndRedirect();
            }
        });

        if(AppGlobals.facebookUserData != null) {
            TextView birthDateView = findViewById(R.id.user_birthdate_input);
            try {
                String birthDateString = AppGlobals.facebookUserData.birthDate;
                //TODO : MM/dd/yyyy change to dd/MM/yyyy
                birthDateView.setText(birthDateString);
            }
            catch (Exception e) {
                Log.w("birthdate_p_ex", e.getMessage());
            }
        }

        //Date Input
        date = (EditText)findViewById(R.id.user_birthdate_input);

        TextWatcher tw = new TextWatcher() {

            private String current = "";
            private String ddmmyyyy = "GGAAYYYY";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                        cal.set(Calendar.MONTH, mon-1);
                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    date.setText(current);
                    date.setSelection(sel < current.length() ? sel : current.length());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        //
        date.addTextChangedListener(tw);
    }

    private void showAgeRestrictionDialog() {
        new CustomDialog(this, getResources().getString(R.string.profile_birthdate_activity_restriction_dialog), R.drawable.warning, true, null,  false, false, 0, null).show();
    }

    private void showWrongText() {
        new CustomDialog(this, getResources().getString(R.string.profile_birthdate_activity_restriction_wrong_text_dialog), R.drawable.warning, true, null,false, false, 0, null).show();
    }

    private void GetBirthdateAndRedirect() {
        //TODO : GET LOGIN DATA!!!
        //AppGlobals.hasLoginData = true;
        //

        EditText btText = (EditText) findViewById(R.id.user_birthdate_input);
        String birthDate = btText.getText().toString();

        if(birthDate == null || birthDate.equals("")) {
            showWrongText();
            return;
        }

        char[] bdChars = birthDate.toCharArray();
        for(char c : bdChars) {
            if (Character.isLetter(c) || Character.isSpaceChar(c)) {
                showWrongText();
                return;
            }
        }

        Boolean isUnderAge = false;
        try {
            Date birthDateDateFormat = new SimpleDateFormat("dd/MM/yyyy").parse(birthDate);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -18);
            Date date18yearsBefore = cal.getTime();


            isUnderAge = birthDateDateFormat.after(date18yearsBefore);
        }
        catch (Exception e) {
            Log.e("DateParseE", e.getMessage());
            return;
        }

        if(isUnderAge) {
            //Toast.makeText(this, "18 yaşından küçükmüşsün, üye yapamıyoruz maalesef :(", Toast.LENGTH_SHORT).show();
            showAgeRestrictionDialog();
            return;
        }

        UserProfileDataObject.userBirthDate = birthDate;

        RedirectTo(ProfileGenderActivity.class);
    }
}
