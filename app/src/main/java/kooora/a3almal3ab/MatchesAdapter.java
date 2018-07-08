//this class assumes that matches are ordered in ascending order regardless of the league



package kooora.a3almal3ab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by McLovin on 5/06/2018.
 */

public class MatchesAdapter extends ArrayAdapter<MatchDataModel>
{
    //this class assumes that matches are ordered in ascending order regardless of the league
    private ArrayList<MatchDataModel> matches;
    private Context context;

    MatchesAdapter(@NonNull Context context)//, ArrayList<MatchDataModel> matches)
    {
        super(context, R.layout.match_item);
        this.matches = new ArrayList<>();
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return matches.size();
    }

    @Nullable
    @Override
    public MatchDataModel getItem(int position)
    {
        return matches.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        //this class assumes that matches are ordered in ascending order regardless of the league
        MatchDataModel match = matches.get(position);

        if (convertView == null)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(context);


            //championship identifier item
            if (match.getTeamA() == null)
            {
                convertView = layoutInflater.inflate(R.layout.championship_item, parent, false);
                ((ImageView)convertView.findViewById(R.id.championsip_logo)).setImageResource(match.getChampionsipDrawable());
                ((TextView)convertView.findViewById(R.id.championsip_name)).setText(match.getChampionship());
                return convertView;
            }

            //match item
            else
            {
                convertView = layoutInflater.inflate(R.layout.match_item, parent, false);

                //i everytime i listview.findViewById(R.id.live_stream_butt)
                renameLiveButts((TextView)convertView.findViewById(R.id.live_stream_butt), (TextView)convertView.findViewById(R.id.backup_live_stream_butt));

                ((TextView)convertView.findViewById(R.id.team_a_name)).setText(match.getTeamA());

                ((ImageView)convertView.findViewById(R.id.team_a_logo)).setImageResource(match.getTeamADrawable());

                ((TextView)convertView.findViewById(R.id.team_b_name)).setText(match.getTeamB());

                ((ImageView)convertView.findViewById(R.id.team_b_logo)).setImageResource(match.getTeamBDrawable());

                ((TextView)convertView.findViewById(R.id.score_time_or_result)).setText(match.getMatchScoreOrTime());

                ((TextView)convertView.findViewById(R.id.match_status)).setText(match.getMatchStatus());

                if (match.getTimeZone() == null)
                    convertView.findViewById(R.id.time_zone).setVisibility(View.INVISIBLE);
                    //((TextView)convertView.findViewById(R.id.time_zone)).setText(Integer.toString(match.getRemainingMins()) + " min remaining");
                else
                    ((TextView)convertView.findViewById(R.id.time_zone)).setText(match.getTimeZone());

                if (match.getCompetitionInfoTxt() != null && !match.getCompetitionInfoTxt().equals(""))
                    ((TextView)convertView.findViewById(R.id.competition_info_txt_vw)).setText(match.getCompetitionInfoTxt());
                else
                    convertView.findViewById(R.id.competition_info_txt_vw).setVisibility(View.GONE);

                if (match.getCompetitionInfoStringImg() != null && !match.getCompetitionInfoStringImg().equals(""))
                {
                    byte[] imgBytes = Base64.decode(match.getCompetitionInfoStringImg().getBytes(), Base64.DEFAULT);
                    Bitmap imgBitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
                    ((ImageView) convertView.findViewById(R.id.competition_info_img_vw)).setImageBitmap(imgBitmap);
                }
                else
                    convertView.findViewById(R.id.competition_info_img_vw).setVisibility(View.GONE);

                return convertView;
            }
        }
        else
            return convertView;
    }

    ArrayList<MatchDataModel> getMatches() { return matches;}

    void swapMatchesArray(ArrayList<MatchDataModel> matches)
    {
        this.matches.clear();
        this.matches.addAll(matches);
        notifyDataSetChanged();
    }

    void renameLiveButts(final TextView live, final TextView backupLive)
    {
        FirebaseDatabase.getInstance().getReference().child("LiveButts").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                String[] values = dataSnapshot.getValue().toString().split(",");
                if (values[0].equals("true"))
                {
                    live.setVisibility(View.VISIBLE);
                    live.setText(values[1]);
                    backupLive.setVisibility(View.VISIBLE);
                    backupLive.setText(values[2]);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
