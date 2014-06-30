package com.codepath.apps.RKTwitterClient.util;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Util {

    static Typeface robotoMedium;

    public static Typeface getRobotoMedium(Context context) {
        if (robotoMedium == null) {
            robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto/Roboto-Medium.ttf");
        }
        return robotoMedium;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView
     * Also from SO: http://stackoverflow.com/a/17503823/143913
     * ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void setupTextviewContents(View parentView, int textViewID, String textValue) {
        TextView tvUserName = (TextView) parentView.findViewById(textViewID);
        tvUserName.setText(textValue);
    }


    /**
     * Update the drawable, bypassing Android null drawable bug.
     * Re-enable/disable your title after setting background
     */
    public static void setActionBarDrawable(ActionBar actionBar, ColorDrawable drawable) {
        actionBar.setBackgroundDrawable(drawable);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
    }
}
