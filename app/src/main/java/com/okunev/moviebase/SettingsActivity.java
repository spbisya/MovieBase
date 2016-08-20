package com.okunev.moviebase;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Project MovieBase. Created by gwa on 8/7/16.
 */

public class SettingsActivity extends AppCompatActivity {
    private AccountHeader headerResult = null;
    private Drawer result = null;
    @BindView(R.id.checkBox)
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        createDrawer(savedInstanceState);
        checkBox.setChecked(getSharedPreferences("MovieBase", 0).getBoolean("autoSync", false));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getSharedPreferences("MovieBase", 0).edit().putBoolean("autoSync", b).apply();
            }
        });
    }

    void createDrawer(Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            getSharedPreferences("MovieBase", 0).edit().putString("tvSeason", email).apply();
            Uri photoUrl = user.getPhotoUrl();
            // User is signed in

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            try {
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            } catch (Exception ignored) {
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Window w = getWindow(); // in Activity's onCreate() for instance
                w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
            initDrawableLoader();
            final IProfile profile = new ProfileDrawerItem()
                    .withName(name)
                    .withEmail(email)
                    .withTextColor(Color.parseColor("#FFFFFF"))
                    .withSelectedTextColor(Color.parseColor("#FFFFFF"));
            if (photoUrl != null)
                profile.withIcon(photoUrl);
            else
                profile.withIcon(getResources().getDrawable(R.drawable.promo));

            headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withCompactStyle(true)
                    .withTextColor(Color.parseColor("#FFFFFF"))
                    .withHeaderBackground(R.drawable.header)
                    .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER)
                    .addProfiles(
                            profile
                    )
                    .withSavedInstance(savedInstanceState)
                    .build();

            //Create the drawer
            result = new DrawerBuilder()
                    .withActivity(this)
                    .withToolbar(toolbar)
                    .withHasStableIds(true)
                    .withAccountHeader(headerResult)
                    .withDisplayBelowStatusBar(true)
                    .addDrawerItems(
                            new PrimaryDrawerItem().withName("Movies list").withIcon(FontAwesome.Icon.faw_film).withIdentifier(1).withSelectable(false),
//                            new PrimaryDrawerItem().withName("New Movie").withIcon(FontAwesome.Icon.faw_plus_circle).withIdentifier(2).withSelectable(false),
                            new DividerDrawerItem(),
                            new PrimaryDrawerItem().withName("Settings").withIcon(FontAwesome.Icon.faw_cog).withIdentifier(3).withSelectable(false),
                            new PrimaryDrawerItem().withName("About").withIcon(FontAwesome.Icon.faw_info_circle).withIdentifier(4).withSelectable(false)
//                            new PrimaryDrawerItem().withName("Log out").withIcon(FontAwesome.Icon.faw_clock_o).withIdentifier(5).withSelectable(false)
                    ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            if (drawerItem != null) {
                                if (drawerItem.getIdentifier() == 2) {
                                    Bundle dataBundle = new Bundle();
                                    dataBundle.putInt("id", 0);
                                    Intent i = new Intent(SettingsActivity.this, DisplayMovieActivity.class);
                                    i.putExtras(dataBundle);
                                    startActivity(i);
                                    finish();
                                } else if (drawerItem.getIdentifier() == 1) {
                                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                                    finish();
                                } else if (drawerItem.getIdentifier() == 4) {
                                    startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                                    finish();
                                }
//                                else if (drawerItem.getIdentifier() == 5) {
//                                    startActivity(new Intent(SettingsActivity.this, AuthActivity.class).putExtra("logout", true));
//                                    finish();
//                                }
                            }
                            return false;
                        }
                    })
                    .withSavedInstance(savedInstanceState)
                    .withSelectedItem(3)
                    .build();
        } else {
            startActivity(new Intent(SettingsActivity.this, AuthActivity.class).putExtra("logout", false));
            finish();
            // No user is signed in
        }

    }

    public void initDrawableLoader() {
        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });
    }

}
