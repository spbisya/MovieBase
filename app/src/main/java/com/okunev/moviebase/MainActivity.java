package com.okunev.moviebase;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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
import com.okunev.moviebase.utils.DBHelper;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.listView1)
    ListView obj;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    ProgressDialog dialog;

    DBHelper mydb;
    private AccountHeader headerResult = null;
    private Drawer result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        createDrawer(savedInstanceState);
        mydb = new DBHelper(this);
        dialog = new ProgressDialog(this);
        if (getSharedPreferences("MovieBase", 0).getBoolean("isJustUpdated", true))
            try {
                getSharedPreferences("MovieBase", 0).edit().putBoolean("isJustUpdated", false).apply();
                mydb.migrateTables(this);
            } catch (Exception ignoreds) {
                Log.d("DRE", "error is " + ignoreds.getMessage());
            }
        setListView();
        if (getSharedPreferences("MovieBase", 0).getBoolean("about", true)) {
            getSharedPreferences("MovieBase", 0).edit().putBoolean("about", false).apply();
            startActivity(new Intent(this, AboutActivity.class));
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddActivity();
            }
        });
        if (getIntent().getBooleanExtra("start", false))
            syncData();
        if (getSharedPreferences("MovieBase", 0).getBoolean("isFirstLaunch", true)) {
            syncData();
            getSharedPreferences("MovieBase", 0).edit().putBoolean("isFirstLaunch", false).apply();
        }


    }

    public void setListView() {
        mydb = new DBHelper(this);
        ArrayList array_list = mydb.getAllMovieNames();
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.list_item, array_list);
        if (array_list.size() != 0) textView.setVisibility(View.INVISIBLE);
        if (obj != null) {
            obj.setAdapter(arrayAdapter);
            obj.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    int id_To_Search = arg2 + 1;

                    Bundle dataBundle = new Bundle();
                    dataBundle.putInt("id", id_To_Search);
                    Intent intent = new Intent(getApplicationContext(), DisplayMovieActivity.class);

                    intent.putExtras(dataBundle);
                    startActivity(intent);
                }
            });
            obj.setOnScrollListener(new AbsListView.OnScrollListener() {
                private int mLastFirstVisibleItem;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {

                    if (mLastFirstVisibleItem < firstVisibleItem) {
                        Log.i("SCROLLING DOWN", "TRUE");
                        fab.hide();
                    }
                    if (mLastFirstVisibleItem > firstVisibleItem) {
                        fab.show();
                        Log.i("SCROLLING UP", "TRUE");
                    }
                    mLastFirstVisibleItem = firstVisibleItem;

                }
            });
        }
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return super.onKeyDown(keycode, event);
    }

    void createDrawer(Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            getSharedPreferences("MovieBase", 0).edit().putString("email", email).apply();
            Uri photoUrl = user.getPhotoUrl();
            // User is signed in

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            try {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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
                                    Intent i = new Intent(MainActivity.this, DisplayMovieActivity.class);
                                    i.putExtras(dataBundle);
                                    startActivity(i);
                                } else if (drawerItem.getIdentifier() == 3) {
                                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                                    finish();
                                } else if (drawerItem.getIdentifier() == 4) {
                                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                                    finish();
                                }
//                                else if (drawerItem.getIdentifier() == 5) {
//                                    startActivity(new Intent(MainActivity.this, AuthActivity.class).putExtra("logout", true));
//                                    finish();
//                                }
                            }
                            return false;
                        }
                    })
                    .withSavedInstance(savedInstanceState)
                    .withSelectedItem(1)
                    .build();

        } else {
            startActivity(new Intent(MainActivity.this, AuthActivity.class).putExtra("logout", false));
            finish();
            // No user is signed in
        }

    }

    public void openAddActivity() {
        Bundle dataBundle = new Bundle();
        dataBundle.putInt("id", 0);
        Intent i = new Intent(MainActivity.this, DisplayMovieActivity.class);
        i.putExtras(dataBundle);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            View sharedView = fab;
            String transitionName = getString(R.string.view_name);
            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, sharedView, transitionName);
            startActivity(i, transitionActivityOptions.toBundle());
        } else {
            startActivity(i);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sync) {
            syncData();
        }
        return super.onOptionsItemSelected(item);
    }

    public void uploadData() {
        String email = getSharedPreferences("MovieBase", 0).getString("email", "");
        if (isConnected())
            if (!email.equals("")) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://movie-base-a33d3.appspot.com");
                StorageReference dbRef = storageRef.child(email + "/databases/MovieBase.db");
                File file2 = new File("//data/data/com.okunev.moviebase/databases/MovieBase.db");
                Uri file = Uri.fromFile(file2);
                UploadTask uploadTask = dbRef.putFile(file);
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d("DRE", "Uh-oh, error! " + exception.getMessage());
                        Toast.makeText(MainActivity.this, "Uh-oh, error! " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("DRE", "download url = " + downloadUrl.toString());
                        if (dialog.isShowing()) dialog.hide();
                        Long millis = Calendar.getInstance().getTimeInMillis();
                        getSharedPreferences("MovieBase", 0).edit().putLong("server_updated_at", millis).putLong("local_updated_at", millis).apply();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Can't retrieve email!", Toast.LENGTH_SHORT).show();
            }
        else
            Toast.makeText(MainActivity.this, "Check Internet connection!", Toast.LENGTH_SHORT).show();
    }

    public void uploadData2() {
        String email = getSharedPreferences("MovieBase", 0).getString("email", "");
        if (isConnected())
            if (!email.equals("")) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://movie-base-a33d3.appspot.com");
                StorageReference dbRef = storageRef.child(email + "/databases/MovieBase.db");
                File file2 = new File("//data/data/com.okunev.moviebase/databases/MovieBase.db");
                Uri file = Uri.fromFile(file2);
                UploadTask uploadTask = dbRef.putFile(file);
                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d("DRE", "Uh-oh, error! " + exception.getMessage());
                        Toast.makeText(MainActivity.this, "Uh-oh, error! " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("DRE", "download url = " + downloadUrl.toString());
                        Long millis = Calendar.getInstance().getTimeInMillis();
                        getSharedPreferences("MovieBase", 0).edit().putLong("server_updated_at", millis).putLong("local_updated_at", millis).apply();
                        syncData();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Can't retrieve email!", Toast.LENGTH_SHORT).show();
            }
        else
            Toast.makeText(MainActivity.this, "Check Internet connection!", Toast.LENGTH_SHORT).show();
    }

    public void downloadData() {
        String email = getSharedPreferences("MovieBase", 0).getString("email", "");
        if (isConnected())
            if (!email.equals("")) {
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://movie-base-a33d3.appspot.com");
                StorageReference dbRef = storageRef.child(email + "/databases/MovieBase.db");
                File localfile = new File("//data/data/com.okunev.moviebase/databases/MovieBase.db");
                dbRef.getFile(localfile).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d("DRE", "err is " + exception.getMessage());
                        Toast.makeText(MainActivity.this, "err is " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d("DRE", "success!");
                        Long millis = Calendar.getInstance().getTimeInMillis();
                        getSharedPreferences("MovieBase", 0).edit().putLong("server_updated_at", millis).putLong("local_updated_at", millis).apply();
                        setListView();
                        if (dialog.isShowing()) dialog.hide();
                        // Local temp file has been created
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Can't retrieve email!", Toast.LENGTH_SHORT).show();
            }
        else
            Toast.makeText(MainActivity.this, "Check Internet connection!", Toast.LENGTH_SHORT).show();
    }

    public void syncData() {
        String email = getSharedPreferences("MovieBase", 0).getString("email", "");
        if (isConnected())
            if (!email.equals("")) {
                dialog.setMessage("Updating database, be patient please!");
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://movie-base-a33d3.appspot.com");
                StorageReference dbRef = storageRef.child(email + "/databases/MovieBase.db");
                dbRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        getSharedPreferences("MovieBase", 0).edit().putLong("server_updated_at", storageMetadata.getUpdatedTimeMillis()).apply();
                        Long local = getSharedPreferences("MovieBase", 0).getLong("local_updated_at", 0);
                        Long server = getSharedPreferences("MovieBase", 0).getLong("server_updated_at", 0);
                        Log.d("DRE", "local " + local + " server " + server + " " + (local > server));
                        if (local > server)
                            uploadData();
                        else
                            downloadData();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                        Log.d("DRE", exception.getMessage());
                        if (exception.getMessage().contains("not exist"))
                            uploadData2();
                        else
                            Toast.makeText(MainActivity.this, "Can't sync - " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Can't retrieve email!", Toast.LENGTH_SHORT).show();
            }
        else
            Toast.makeText(MainActivity.this, "Check Internet connection!", Toast.LENGTH_SHORT).show();
    }

    public final boolean isConnected() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            // if connected with internet
            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {

            return false;
        }
        return false;
    }
}