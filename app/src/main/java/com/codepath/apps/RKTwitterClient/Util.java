package com.codepath.apps.RKTwitterClient;

import android.view.View;
import android.widget.TextView;

public class Util {

    public static void setupTextviewContents(View parentView, int textViewID, String textValue) {
        TextView tvUserName = (TextView) parentView.findViewById(textViewID);
        tvUserName.setText(textValue);
    }


}
