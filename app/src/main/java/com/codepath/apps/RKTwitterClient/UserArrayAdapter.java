package com.codepath.apps.RKTwitterClient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codepath.apps.RKTwitterClient.models.User;

import java.util.List;

public class UserArrayAdapter extends ArrayAdapter<User> {

    public UserArrayAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final User user = getItem(position);
        View inflatedView;
        if (convertView == null) {
            LayoutInflater inflator = LayoutInflater.from(getContext());
            inflatedView = inflator.inflate(R.layout.user_item, parent, false);
        }
        else {
            inflatedView = convertView;
        }

        TextView tvUser = (TextView)inflatedView.findViewById(R.id.tvUserName);
        tvUser.setText(user.getName());

        TextView tvScreen = (TextView)inflatedView.findViewById(R.id.tvUserScreenName);
        tvScreen.setText(user.getScreenName());

        return inflatedView;
    }

}
