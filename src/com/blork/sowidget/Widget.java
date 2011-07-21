package com.blork.sowidget;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Widget extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.d("sowidget","onUpdate.");          
    	context.startService(new Intent(context, SoService.class));  
    }
     
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	Log.d("sowidget","Received broadcast.");
    	context.startService(new Intent(context, SoService.class));
    }
}