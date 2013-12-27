package com.abplus.surroundcalc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.abplus.surroundcalc.models.Drawing;

public class MainActivity extends Activity implements ActionBar.TabListener {

    DoodleView doodleView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        doodleView = (DoodleView) findViewById(R.id.doddle_view);

        ActionBar actionBar = getActionBar();

        addTab(actionBar, Drawing.KeyColor.BLUE,   true);
        addTab(actionBar, Drawing.KeyColor.GREEN,  false);
        addTab(actionBar, Drawing.KeyColor.RED,    false);
        addTab(actionBar, Drawing.KeyColor.YELLOW, false);

        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }
    }

    private void addTab(ActionBar actionBar, Drawing.KeyColor keyColor, boolean selected) {
        ActionBar.Tab tab = actionBar.newTab();

        int resId = R.string.blue;
        switch (keyColor) {
            case BLUE:
                resId = R.string.blue;
                break;
            case GREEN:
                resId = R.string.green;
                break;
            case RED:
                resId = R.string.red;
                break;
            case YELLOW:
                resId = R.string.yellow;
                break;
        }
        Drawing drawing = new Drawing(keyColor);

        tab.setText(resId);
        tab.setTabListener(this);
        tab.setTag(drawing);
        actionBar.addTab(tab, selected);

        if (selected) {
            doodleView.setDrawing(drawing);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        doodleView.redraw();
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        Drawing drawing = (Drawing) tab.getTag();
        doodleView.setDrawing(drawing);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
//            case android.R.id.home:
//                return true;
            case R.id.action_clear_drawing:
                doodleView.clear();
                return true;
        }
        return false;
    }
}
