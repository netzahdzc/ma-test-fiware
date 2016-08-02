/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.inger.android.ollintest;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.inger.android.ollintest.util.PatientDBHandlerUtils;
import com.inger.android.ollintest.util.SessionUtil;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadActivityData();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivityData();
    }

    private void addDrawerItems() {
        final ArrayList<LeftMenuItem> leftMenuItems = new ArrayList<LeftMenuItem>();
        leftMenuItems.add(new LeftMenuItem(R.mipmap.ic_home, getResources().getString(R.string.left_menu_home)));
        leftMenuItems.add(new LeftMenuItem(R.mipmap.ic_setting, getResources().getString(R.string.left_menu_setting)));
        leftMenuItems.add(new LeftMenuItem(R.mipmap.ic_info, getResources().getString(R.string.left_menu_help)));
        leftMenuItems.add(new LeftMenuItem(R.mipmap.ic_exit, getResources().getString(R.string.left_menu_logout)));

        LeftMenuAdapter adapter = new LeftMenuAdapter(this, leftMenuItems, R.color.tan_background);
        mDrawerList.setAdapter(adapter);

        // Set a click listener to open section with detail data from respective patient when the list item is clicked on
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    //Home option
                    case 0:
                        //No action
                        break;
                    //Settings option
                    case 1: {
                        Intent settingScreen = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(settingScreen);
                    }
                    break;
                    //Help option
                    case 2: {
                        Intent faqScreen = new Intent(getApplicationContext(), FAQsListViewActivity.class);
                        startActivity(faqScreen);
                    }
                    break;
                    //Logout option
                    case 3: {
                        logout();
                    }
                    break;
                    //Close options including exit/logout
                    default: {
                        finish();
                    }
                }
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerList);

            }
        });

    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getResources().getString(R.string.left_menu_title));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void loadData(Cursor cursor) {
        // Create a list of patients
        final ArrayList<Patient> patients = new ArrayList<Patient>();

        // Create an {@link PatientAdapter}, whose data source is a list of {@link Patient}s. The
        // adapter knows how to create list items for each item in the list.
        PatientAdapter adapter = new PatientAdapter(this, patients, R.color.tan_background);

        // Find the {@link ListView} object in the view hierarchy of the {@link Activity}.
        // There should be a {@link ListView} with the view ID called list, which is declared in the
        ListView listView = (ListView) findViewById(R.id.list);

        // Make the {@link ListView} use the {@link PatientAdapter} we created above, so that the
        // {@link ListView} will display list items for each {@link Patient} in the list.
        listView.setAdapter(adapter);
        try {
            if (cursor.moveToFirst()) {
                do {
                    long mUniqueParticipantId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(DatabaseContract.Patient._ID)
                    );

                    String mParticipantName = cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL1)
                    );

                    String mParticipantSurname = cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL2)
                    );

                    int mParticipantGender = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL3)
                    );

                    String mParticipantBirthday = cursor.getString(
                            cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL4)
                    );

                    byte[] mParticipantPhoto = cursor.getBlob(
                            cursor.getColumnIndexOrThrow(DatabaseContract.Patient.COLUMN_NAME_COL5)
                    );

                    // do what ever you want here
                    patients.add(new Patient(mUniqueParticipantId, mParticipantName, mParticipantSurname,
                            mParticipantGender, mParticipantBirthday, mParticipantPhoto));

                    // Set a click listener to open section with detail data from respective patient when the list item is clicked on
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            // Get the {@link Patient} object at the given position the user clicked on
                            Patient patientObj = patients.get(position);

                            // Activate Patient id on db to be reachable from any application screen
                            SessionUtil sessionObj = new SessionUtil(getApplicationContext());
                            sessionObj.openDB();
                            sessionObj.setPatientSession(patientObj.getUniquePatientId());
                            sessionObj.closeDB();

                            Intent patientFolder = new Intent(getApplicationContext(), PatientFolderActivity.class);
                            startActivity(patientFolder);
                        }
                    });

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // exception handling
        } finally {
            if (cursor != null) {
//                cursor.close();
            }
        }
    }

    public void loadActivityData() {
        PatientDBHandlerUtils patientDBObj = new PatientDBHandlerUtils(getApplicationContext());
        SessionUtil sessionObj = new SessionUtil(getApplicationContext());
        sessionObj.openDB();
        patientDBObj.openDB();

        Cursor mCursorPatient = patientDBObj.readAllData();
        loadData(mCursorPatient);

        mDrawerList = (ListView) findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        sessionObj.closeDB();
        patientDBObj.closeDB();
    }

    public void logout() {
        SessionUtil sessionObj = new SessionUtil(getApplicationContext());
        sessionObj.openDB();

        //Write logout on database
        sessionObj.resetUserSession();
        sessionObj.closeDB();

        //Get rid of activities
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("finish", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_patient: {
                // Create a new intent to open the {@link IncreaseActivity}
                Intent increaseIntent = new Intent(MainActivity.this, IncreaseActivity.class);
                startActivity(increaseIntent);
            }
            default: {
                //Activate the navigation drawer toggle
                if (mDrawerToggle.onOptionsItemSelected(item)) {
                    return true;
                }
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
            }
        }

    }

    @Override
    public void onBackPressed() {
    }

}
