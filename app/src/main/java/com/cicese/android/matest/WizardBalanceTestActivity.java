package com.cicese.android.matest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.cicese.android.matest.fragment.WizardBalanceTestOptionOneLegFragment;
import com.cicese.android.matest.fragment.WizardBalanceTestOptionSemiTandemFragment;
import com.cicese.android.matest.fragment.WizardBalanceTestOptionTandemFragment;
import com.cicese.android.matest.fragment.WizardBalanceTestOptionTogetherFragment;


public class WizardBalanceTestActivity extends ActionBarActivity {

    private MyPagerAdapter adapter;
    private ViewPager pager;
    private TextView nextButton;
    private TextView skipButton;
    private TextView navigator;
    private int currentItem;
    private int testType;
    private int balanceTestOption;

    final int WALKING_TEST = 1;
    final int STRENGTH_TEST = 2;
    final int BALANCE_TEST = 3;

    final int TANDEM_TEST_OPTION = 1;
    final int SEMI_TANDEM_TEST_OPTION = 2;
    final int FEET_TOGETHER_TEST_OPTION = 3;
    final int ONE_LEG_TEST_OPTION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wizard_social);

        currentItem = 0;
        testType = getIntent().getIntExtra("testType", 0);
        balanceTestOption = getIntent().getIntExtra("balanceTestOption", 0);

        pager = (ViewPager) findViewById(R.id.activity_wizard_social_pager);
        nextButton = (TextView) findViewById(R.id.activity_wizard_social_next);
        skipButton = (TextView) findViewById(R.id.activity_wizard_social_skip);
        navigator = (TextView) findViewById(R.id.activity_wizard_social_position);

        adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(currentItem);
        setNavigator();

        pager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int position) {
                if (pager.getCurrentItem() == (pager.getAdapter().getCount() - 1)) {
                    nextButton.setText(getResources().getString(R.string.button_continue_test));
                } else {
                    nextButton.setText(getResources().getString(R.string.button_next));
                }
                setNavigator();
            }
        });

        skipButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent countDownScreen = new Intent(getApplicationContext(), CountDownActivity.class);
                countDownScreen.putExtra("testType", testType);
                countDownScreen.putExtra("balanceTestOption", balanceTestOption);
                startActivity(countDownScreen);
            }
        });

        nextButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (pager.getCurrentItem() != (pager.getAdapter().getCount() - 1)) {
                    pager.setCurrentItem(pager.getCurrentItem() + 1);
                } else {
                    Intent countDownScreen = new Intent(getApplicationContext(), CountDownActivity.class);
                    countDownScreen.putExtra("testType", testType);
                    countDownScreen.putExtra("balanceTestOption", balanceTestOption);
                    startActivity(countDownScreen);
                }
                setNavigator();
            }
        });

    }

    public void setNavigator() {
        String navigation = "";
        for (int i = 0; i < adapter.getCount(); i++) {
            if (i == pager.getCurrentItem()) {
                navigation += getString(R.string.material_icon_box_full) + "  ";
            } else {
                navigation += getString(R.string.material_icon_check_empty) + "  ";
            }
        }
        navigator.setText(navigation);
    }

    public void setCurrentSlidePosition(int position) {
        this.currentItem = position;
    }

    public int getCurrentSlidePosition() {
        return this.currentItem;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

        @Override
        public int getCount() {
            int temp = 0;
            switch (testType) {
                case WALKING_TEST:
                    temp = 8;
                    break;
                case STRENGTH_TEST:
                    temp = 6;
                    break;
                case BALANCE_TEST:
                    temp = 4;
                    break;
                default:
            }
            return temp;
        }

        @Override
        public Fragment getItem(int position) {
            if (balanceTestOption == TANDEM_TEST_OPTION) {
                return WizardBalanceTestOptionTandemFragment.newInstance(position);
            } else if (balanceTestOption == SEMI_TANDEM_TEST_OPTION) {
                return WizardBalanceTestOptionSemiTandemFragment.newInstance(position);
            } else if (balanceTestOption == FEET_TOGETHER_TEST_OPTION) {
                return WizardBalanceTestOptionTogetherFragment.newInstance(position);
            } else {
                return WizardBalanceTestOptionOneLegFragment.newInstance(position);
            }
        }//End of fragment function
    }
}