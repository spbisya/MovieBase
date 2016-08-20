package com.okunev.moviebase;

/**
 * Created by 777 on 2/14/2016.
 */

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.okunev.moviebase.models.Movie;
import com.okunev.moviebase.utils.DBHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DisplayMovieActivity extends AppCompatActivity {
    private DBHelper mydb;

    @BindView(R.id.editName)
    TextView tvName;
    @BindView(R.id.editPart)
    TextView tvPart;
    @BindView(R.id.editSeason)
    TextView tvSeason;
    @BindView(R.id.editSeries)
    TextView tvSeries;
    @BindView(R.id.editTime)
    TextView tvTime;
    @BindView(R.id.edit)
    ImageView edit;
    @BindView(R.id.save)
    ImageView save;
    @BindView(R.id.delete)
    ImageView delete;
    int id_To_Update = 0;
    private AccountHeader headerResult = null;
    private Drawer result = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_movie);
        ButterKnife.bind(this);
        createDrawer(savedInstanceState);
        mydb = new DBHelper(this);
        ArrayList<Integer> list = mydb.getAllId();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int Value = extras.getInt("id");

            if (Value > 0) {
                Value -= 1;
                //means this is the view tvPart not the add contact tvPart.
                Movie movie = mydb.getData(list.get(Value));
                id_To_Update = list.get(Value);

                tvName.setText(movie.getName());
                tvPart.setText(movie.getPart());
                tvSeason.setText(movie.getSeason());
                tvSeries.setText(movie.getSeries());
                tvTime.setText(movie.getTime());
                setViews(false);
                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setViews(true);
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayMovieActivity.this);
                        builder.setMessage(R.string.deleteContact)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mydb.deleteMovie(id_To_Update);
                                        Toast.makeText(getApplicationContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class).putExtra("start", getSharedPreferences("MovieBase", 0).getBoolean("autoSync", false));
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });
                        AlertDialog d = builder.create();
                        d.setTitle("Are you sure?");
                        d.show();
                    }
                });
            } else {
                setViews(true);
            }
        }
    }

    public void setViews(Boolean isEditing) {
        tvName.setClickable(isEditing);
        tvName.setFocusable(isEditing);
        tvName.setFocusableInTouchMode(isEditing);
        tvPart.setFocusable(isEditing);
        tvPart.setClickable(isEditing);
        tvPart.setFocusableInTouchMode(isEditing);
        tvSeason.setFocusable(isEditing);
        tvSeason.setClickable(isEditing);
        tvSeason.setFocusableInTouchMode(isEditing);
        tvSeries.setFocusable(isEditing);
        tvSeries.setClickable(isEditing);
        tvSeries.setFocusableInTouchMode(isEditing);
        tvTime.setFocusable(isEditing);
        tvTime.setClickable(isEditing);
        tvTime.setFocusableInTouchMode(isEditing);
        save.setVisibility(isEditing ? View.VISIBLE : View.INVISIBLE);
        edit.setVisibility(isEditing ? View.INVISIBLE : View.VISIBLE);
        delete.setVisibility(isEditing ? View.INVISIBLE : View.VISIBLE);
    }

    public void run(View view) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int Value = extras.getInt("id");
            if (Value > 0) {
                if (mydb.updateMovie(id_To_Update, tvName.getText().toString(), tvPart.getText().toString(), tvSeason.getText().toString(), tvSeries.getText().toString(), tvTime.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Not Updated", Toast.LENGTH_SHORT).show();
                }
                getSharedPreferences("MovieBase", 0).edit().putLong("local_updated_at", Calendar.getInstance().getTimeInMillis()).apply();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class).putExtra("start", getSharedPreferences("MovieBase", 0).getBoolean("autoSync", false));
                startActivity(intent);
            } else {
                if (!tvName.getText().toString().equals("")) {
                    if (mydb.insertMovie(tvName.getText().toString(), tvPart.getText().toString(), tvSeason.getText().toString(), tvSeries.getText().toString(), tvTime.getText().toString())) {
                        Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Not done", Toast.LENGTH_SHORT).show();
                    }
                    getSharedPreferences("MovieBase", 0).edit().putLong("local_updated_at", Calendar.getInstance().getTimeInMillis()).apply();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class).putExtra("start", getSharedPreferences("MovieBase", 0).getBoolean("autoSync", false));
                    startActivity(intent);
                } else
                    Toast.makeText(getApplicationContext(), "Enter name", Toast.LENGTH_SHORT).show();
            }
        }
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
                                if (drawerItem.getIdentifier() == 1) {
                                    Intent i = new Intent(DisplayMovieActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                } else if (drawerItem.getIdentifier() == 3) {
                                    startActivity(new Intent(DisplayMovieActivity.this, SettingsActivity.class));
                                    finish();
                                } else if (drawerItem.getIdentifier() == 4) {
                                    startActivity(new Intent(DisplayMovieActivity.this, AboutActivity.class));
                                    finish();
                                }
//                                else if (drawerItem.getIdentifier() == 5) {
//                                    startActivity(new Intent(DisplayMovieActivity.this, AuthActivity.class).putExtra("logout", true));
//                                    finish();
//                                }
                            }
                            return false;
                        }
                    })
                    .withSavedInstance(savedInstanceState)
                    .withSelectedItem(2)
                    .build();
        } else {
            startActivity(new Intent(DisplayMovieActivity.this, AuthActivity.class).putExtra("logout", false));
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