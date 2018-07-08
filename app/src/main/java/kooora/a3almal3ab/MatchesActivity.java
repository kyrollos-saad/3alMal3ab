//TODO: organize matches by time under its championship (IDEA: each championship is an adapter in the listview OR is an array in the adapter)
//TODO: fix gradle shit
//TODO: championship separation
//TODO: check the warning on the activity in the manifest
//TODO: show all matches with date separators as well as championship ones
//TODO: prevent duplicates in database (DONE)

//all matches are stored cronologically in the database regrardless of the championship. i handel the championship thing herein the app
//in firebase database key is name and value is value
//drawables are the same indecies as their items array in the resources


package kooora.a3almal3ab;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MatchesActivity extends AppCompatActivity
{
    TabHost tabHost;
    long splashStart;
    DatePickerDialog datePickerDialog;
    ListView listView;
    MatchesAdapter matchesAdapter;
    ArrayList<MatchDataModel> matchesToShow;
    public static ArrayList<MatchDataModel> allMatches = new ArrayList<MatchDataModel>();
    final Context context = this;
    TextView liveButt;
    TextView liveBackupButt;

    Thread splashScreenThread;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference matchesReference = databaseReference.child("matches");

    FirebaseAnalytics firebaseAnalytics;
    Bundle analyticsTempBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);
        MobileAds.initialize(this, "ca-app-pub-4659966632628158~9431829202");//init AdMob service

        //setup TabHost
        tabHost = (TabHost)findViewById(R.id.tab_host);
        tabHost.setForeground(ContextCompat.getDrawable(this, R.drawable.received_10212668698649289));
        splashStart = System.currentTimeMillis();

        tabHost.setup();
        initTabs(tabHost);

        //ads
        AdView topAdVw = (AdView)findViewById(R.id.top_ad_vw);
        AdView bottAdVw = (AdView)findViewById(R.id.bottom_ad_vw);
        topAdVw.loadAd(new AdRequest.Builder().build());
        bottAdVw.loadAd(new AdRequest.Builder().build());

        System.out.println("/// id=" + Settings.Secure.ANDROID_ID);

        //analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putInt(FirebaseAnalytics.Param.INDEX, 1);
        bundle.putInt(FirebaseAnalytics.Param.ITEM_ID, 56734);
        bundle.putString(FirebaseAnalytics.Param.VALUE, "stupid ass value");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);


        ///// buttons /////
        //date butt
        final Button dateButt = findViewById(R.id.date_butt);

        //init date button and datepicker
        final Calendar calendar = Calendar.getInstance();

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
            {
                hideAndShowAccordingToDate(year, month, dayOfMonth);
                dateButt.setText(String.format("%d/%d/%d", dayOfMonth, month + 1, year));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        dateButt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                datePickerDialog.show();
            }
        });

        ///// ListView /////
        //get list view, initialize matches custom adapter, then, set it as the listview's adapter
        listView = (ListView)findViewById(R.id.matches_list);
        matchesAdapter = new MatchesAdapter(this);
        listView.setAdapter(matchesAdapter);

        //firebase database shit
        populateMatchesAdapter();

        //handle listview item clicking
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                final MatchDataModel clickedMatchDataModel = (MatchDataModel)parent.getAdapter().getItem(position);

                liveButt = listView.findViewById(R.id.live_stream_butt);
                liveBackupButt = listView.findViewById(R.id.backup_live_stream_butt);

                liveButt.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String urlToOpen = clickedMatchDataModel.getLiveStreamLink();
                        if (urlToOpen.equals(""))
                        {
                            popAnAlertUp(getString(R.string.error), getString(R.string.no_link_available));
                            return;
                        }
                        if (!urlToOpen.contains("https://"))
                            urlToOpen = "http://" + urlToOpen;
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
                        startActivity(browserIntent);
                    }
                });
                liveBackupButt.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String urlToOpen = clickedMatchDataModel.getLiveStreamLink();

                        if (urlToOpen.equals(""))
                        {
                            popAnAlertUp(getString(R.string.error), getString(R.string.no_link_available));
                            return;
                        }

                        if (!urlToOpen.contains("https://"))
                            urlToOpen = "http://" + urlToOpen;
                        Intent browserIntent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(clickedMatchDataModel.getBackupLiveStreamLink()));
                        startActivity(browserIntent2);
                    }
                });
                listView.findViewById(R.id.competition_info_img_vw).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        competitionWasClicked();
                        analyticsTempBundle = new Bundle();
                        analyticsTempBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "image_click");
                        firebaseAnalytics.logEvent("CompetitionClick", analyticsTempBundle);

                        if (listView.findViewById(R.id.competition_info_img_vw).getVisibility() == View.GONE)
                            return;
                        byte[] imgBytes = clickedMatchDataModel.getCompetitionInfoStringImg().getBytes();
                        imgBytes = Base64.decode(imgBytes, Base64.DEFAULT);
                        Bitmap imgToShow = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);

                        Dialog imgPreviewDialog = new Dialog(context);
                        imgPreviewDialog.setContentView(R.layout.large_image_view);
                        ((ImageView)imgPreviewDialog.findViewById(R.id.large_img_vw)).setImageBitmap(imgToShow);
                        imgPreviewDialog.show();
                    }
                });
                listView.findViewById(R.id.competition_info_txt_vw).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        competitionWasClicked();
                        analyticsTempBundle = new Bundle();
                        analyticsTempBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "text_click");
                        firebaseAnalytics.logEvent("CompetitionClick", analyticsTempBundle);
                    }});
            }
        });






        ///////////////////////////////////// the subscription tab /////////////////////////////////////
        ///////////////////////////////////// the subscription tab /////////////////////////////////////

        final Button subscribeButt = findViewById(R.id.subscribe_butt);
        final EditText
                nameEditText = findViewById(R.id.name_edit_text),
                phoneEditText = findViewById(R.id.phone_number_edit_text);

        final SharedPreferences sharedPreferences = getSharedPreferences("namePhoneNo", MODE_PRIVATE);
        final String s = sharedPreferences.getString("namePhoneNo", null);

        if (s != null)
        {
            final TextView namePhoneNoTextView = findViewById(R.id.name_phone_no);
            namePhoneNoTextView.setText(s);
            namePhoneNoTextView.setVisibility(View.VISIBLE);
            nameEditText.setVisibility(View.GONE);
            phoneEditText.setVisibility(View.GONE);
            subscribeButt.setText(getString(R.string.cancel_subscription));
            subscribeButt.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //delete user
                    databaseReference.child("CompititionUsers").child(s.split("\n")[1]).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            sharedPreferences.edit().remove("namePhoneNo").apply();
                            popAnAlertUp(getString(R.string.done), getString(R.string.youve_unsubscribed_successfully));


                        }
                    });
                }
            });
        }
        else
        {
            subscribeButt.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (nameEditText.getText().toString().equals("") || phoneEditText.getText().toString().equals(""))
                        popAnAlertUp(getString(R.string.error), getString(R.string.you_didnt_fill_all_fields));
                    else
                    {
                        String s = String.format("Name: %s\nPhone no: %s",
                                nameEditText.getText().toString(),
                                phoneEditText.getText().toString());

                        sharedPreferences.edit().putString("namePhoneNo", s).apply();
                        Map<String,Object> newSubscriber = new HashMap<>();
                        newSubscriber.put(phoneEditText.getText().toString(), nameEditText.getText().toString());

                        databaseReference.child("CompititionUsers").updateChildren(newSubscriber).addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                popAnAlertUp(getString(R.string.done), getString(R.string.you_subscribed_successfully));
                            }
                        });
                    }
                }
            });
        }


        //check if the app isn't connected to the internet
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (System.currentTimeMillis() - splashStart < 4000L)
                    ;

                //modifying anything on the UI should be done on the UI thread
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (tabHost.getForeground() != null)
                        {
                            tabHost.setForeground(null);
                            new AlertDialog.Builder(context)
                                    .setTitle("Error")
                                    .setMessage(getString(R.string.you_have_to_connect_to_the_internet))
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {@Override public void onClick(DialogInterface dialog, int which) { finishAffinity(); }}) //exit app
                                    .show();
                        }
                    }
                });
            }
        }).start();
    }

    /************************* for cleaner code *************************/

    private void initTabs(TabHost tabHost)
    {
        //setup matches tab
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("matches_tab");
        tabSpec.setContent(R.id.matches_tab);
        tabSpec.setIndicator(getString(R.string.matches));
        tabHost.addTab(tabSpec);

        /*//setup live stream tab
        tabSpec = tabHost.newTabSpec("live_stream_tab");
        tabSpec.setContent(R.id.live_stream_tab);
        tabSpec.setIndicator(getString(R.string.live_stream));
        tabHost.addTab(tabSpec);*/

        //setup competitions tab
        tabSpec = tabHost.newTabSpec("competitions_tab");
        tabSpec.setContent(R.id.competitions_tab);
        tabSpec.setIndicator(getString(R.string.competitions));
        tabHost.addTab(tabSpec);

        /*//setup pitch booking tab
        tabSpec = tabHost.newTabSpec("pitch_booking_tab");
        tabSpec.setContent(R.id.pitch_booking_tab);
        tabSpec.setIndicator(getString(R.string.pitch_booking));
        tabHost.addTab(tabSpec);*/

        TabWidget tabWidget = (TabWidget)findViewById(android.R.id.tabs);
        for (int i=0; i<tabWidget.getChildCount(); i++)
            ((TextView)tabWidget.getChildAt(i).findViewById(android.R.id.title)).setTextSize(12.0f);
    }

    void populateMatchesAdapter()
    {
        matchesReference.orderByKey().addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //declarations
                Iterator<DataSnapshot> allMatchesIterator = dataSnapshot.getChildren().iterator();
                DataSnapshot tempAllMatchesDataSnapshot;
                long matchDateTimeLong;

                allMatches = new ArrayList<>();
                MatchDataModel matchDataModel;

                while (allMatchesIterator.hasNext())
                {
                    matchDataModel = new MatchDataModel();
                    tempAllMatchesDataSnapshot = allMatchesIterator.next();

                    matchDateTimeLong = Long.valueOf(tempAllMatchesDataSnapshot.getKey());

                    matchDataModel.extractInfoFromDatabase(tempAllMatchesDataSnapshot, matchDateTimeLong);
                    allMatches.add(matchDataModel);
                }
                matchesAdapter.swapMatchesArray(allMatches);
                hideAndShowAccordingToDate(-1,-1,-1);// passing -1 makes it get today's date
                listView.setAdapter(matchesAdapter);//for some reason notifyDataSetChanged() isn't enough here

                //for some reason when the app runs for the second time without swiping from the recent tasks it freezes those 3 seconds and shows a black screen instead of the splash screen
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        while (System.currentTimeMillis() - splashStart < 3000L) ;

                        //modifying anything on the UI should be done on the UI thread
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                tabHost.setForeground(null);
                            }
                        });
                    }
                }).start();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { popAnAlertUp("Error", databaseError.toString()); }
        });
    }

    int clickedValue;
    void competitionWasClicked()
    {
        final DatabaseReference competitionClickRef = databaseReference.child("competitionClick");
        competitionClickRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //get the current value and set it equal to itself + 1, all in one line
                clickedValue = Integer.valueOf(dataSnapshot.getValue().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {/*user shouldn't care about this error*/}
        });
        competitionClickRef.setValue(clickedValue + 1);
    }

    void hideAndShowAccordingToDate(int year, int month, int dayOfMonth)
    {
        if (year == -1)
        {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        }

        month++; //this bitch dialog returns months in 0~11 format
        long millsStart, millsEnd;
        String dayOfmonthStr = String.valueOf(dayOfMonth), dayOfMonthPlusOneStr = String.valueOf(dayOfMonth+1),monthStr = String.valueOf(month), yearStr = String.valueOf(year);

        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        try
        {
            millsStart = f.parse(dayOfmonthStr + '-' + monthStr + '-' + yearStr).getTime();
            millsEnd = f.parse(dayOfMonthPlusOneStr + '-' + monthStr + '-' + yearStr).getTime();

            if (matchesToShow == null)
                matchesToShow = new ArrayList<>();
            else
                matchesToShow.clear();
            Long tempLong;
            for (int i=0; i<allMatches.size(); i++)
            {
                tempLong = allMatches.get(i).getMatchDateTimeLong();
                if (allMatches.get(i).getTeamA() == null || (tempLong > millsStart && tempLong < millsEnd))//teamA == null means that the view is a championship separator
                    matchesToShow.add(allMatches.get(i));
            }
            matchesAdapter.swapMatchesArray(matchesToShow);
            listView.setAdapter(matchesAdapter); //for some reason notifyDataSetChanged() isn't enough here

        } catch (ParseException e) {popAnAlertUp(getString(R.string.error), e.toString());}
    }

    /************************* tools *************************/

    public void popAnAlertUp(String title, Object message)
    {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(String.valueOf(message))
                .show();
    }
}
