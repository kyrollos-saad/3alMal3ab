package kooora.a3almal3ab;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by McLovin on 5/06/2018.
 */

public class MatchDataModel
{
    //even if i don't need some of these attributes now, i might need it later
    private String championship;
    private int championsipDrawable;
    private String teamA, teamB, matchScoreOrTime, matchStatus, timeZone, liveStreamLink, backupLiveStreamLink, competitionInfoTxt, competitionInfoStringImg;
    private int teamADrawable, teamBDrawable;
    private int remainingMins;
    private long matchDateTimeLong;

    private static final Map<String, Integer> teamsLogosArr = new HashMap<String, Integer>()
    {{
        put("اختر فريق", R.mipmap.default_team_logo);
        put("السنغال", R.drawable.sen);
        put("المغرب", R.drawable.mar);
        put("تونس", R.drawable.tun);
        put("مصر", R.drawable.egy);
        put("نيجيريا", R.drawable.nga);
        put("أستراليا", R.drawable.aus);
        put("إيران", R.drawable.irn);
        put("السعودية", R.drawable.ksa);
        put("اليابان", R.drawable.jpn);
        put("كوريا الجنوبية", R.drawable.kor);
        put("أسبانيا", R.drawable.esp);
        put("أيسلندا", R.drawable.isl);
        put("البرتغال", R.drawable.por);
        put("الدنمارك", R.drawable.den);
        put("السويد", R.drawable.swe);
        put("الصرب", R.drawable.srb);
        put("المانيا", R.drawable.ger);
        put("انجلترا", R.drawable.eng);
        put("بلجيكا", R.drawable.bel);
        put("بولندا", R.drawable.pol);
        put("روسيا", R.drawable.rus);
        put("سويسرا", R.drawable.sui);
        put("فرنسا", R.drawable.fra);
        put("كرواتيا", R.drawable.cro);
        put("المكسيك", R.drawable.mex);
        put("بنما", R.drawable.pan);
        put("كوستاريكا", R.drawable.crc);
        put("أوروجواي", R.drawable.uru);
        put("الأرجنتين", R.drawable.arg);
        put("البرازيل", R.drawable.bra);
        put("بيرو", R.drawable.per);
        put("كولومبيا", R.drawable.col);
    }};

    //this indecates that this instance is just a championship header not a match
    MatchDataModel() { this.teamA = null; }

    MatchDataModel(String championship, int championsipDrawable)
    {
        this.championship = championship;
        this.championsipDrawable = championsipDrawable;
        this.teamA = null; //for the MatchesAdapter to detect that this is not a match item
    }

    public MatchDataModel(long matchDateTimeLong, String championship, int championshipDrawable, String matchScoreOrTime, String matchStatus, int remainingMins, String teamA, int teamADrawable, String teamB, int teamBDrawable, String timeZone, String liveStreamLink, String backupLiveStreamLink, String competitionInfoTxt, String competitionInfoStringImg)
    {
        this.championship = championship;
        this.championsipDrawable = championshipDrawable;
        this.matchScoreOrTime = matchScoreOrTime;
        this.matchDateTimeLong = matchDateTimeLong;
        this.matchStatus = matchStatus;
        this.remainingMins = remainingMins;
        this.teamA = teamA;
        this.teamADrawable = teamsLogosArr.get(teamA);
        this.teamB = teamB;
        this.teamBDrawable = teamsLogosArr.get(teamA);
        this.timeZone = timeZone;
        this.liveStreamLink = liveStreamLink;
        this.backupLiveStreamLink = backupLiveStreamLink;
        this.competitionInfoTxt = competitionInfoTxt;
        this.competitionInfoStringImg = competitionInfoStringImg;
    }

    public String getChampionship()
    {
        return championship;
    }

    public int getChampionsipDrawable()
    {
        return championsipDrawable;
    }

    public String getTeamA()
    {
        return teamA;
    }

    public String getTeamB()
    {
        return teamB;
    }

    public long getMatchDateTimeLong()
    {
        return matchDateTimeLong;
    }

    public String getMatchScoreOrTime()
    {
        return matchScoreOrTime;
    }

    public int getRemainingMins()
    {
        return remainingMins;
    }

    public String getMatchStatus()
    {
        return matchStatus;
    }

    public String getTimeZone()
    {
        return timeZone;
    }

    public int getTeamADrawable()
    {
        return teamADrawable;
    }

    public int getTeamBDrawable()
    {
        return teamBDrawable;
    }

    public String getLiveStreamLink()
    {
        return liveStreamLink;
    }

    public String getBackupLiveStreamLink()
    {
        return backupLiveStreamLink;
    }

    public String getCompetitionInfoTxt()
    {
        return competitionInfoTxt;
    }

    public String getCompetitionInfoStringImg()
    {
        return competitionInfoStringImg;
    }

    void extractInfoFromDatabase(DataSnapshot dataSnapshot, Long dateTimeLong)
    {
        Iterator<DataSnapshot> singleMatchIterator = dataSnapshot.getChildren().iterator();
        matchDateTimeLong = dateTimeLong;

        while (singleMatchIterator.hasNext())
        {
            dataSnapshot = singleMatchIterator.next();

            String tempKey = dataSnapshot.getKey();
            String tempValue = (dataSnapshot.getValue() == null) ? "" : dataSnapshot.getValue().toString();

            switch (tempKey)
            {
                case "championship":
                    championship = tempValue;
                    break;
                case "championshipDrawable":
                    championsipDrawable = Integer.valueOf(tempValue);
                    break;
                case "matchScoreOrTime":
                    matchScoreOrTime = tempValue;
                    break;
                case "matchStatus":
                    matchStatus = tempValue;
                    break;
                case "remainingMins":
                    remainingMins = Integer.valueOf(tempValue);
                    break;
                case "teamA":
                    teamA = tempValue;
                    break;
                case "teamADrawable":
                    teamADrawable = teamsLogosArr.get(teamA);
                    break;
                case "teamB":
                    teamB = tempValue;
                    break;
                case "teamBDrawable":
                    teamBDrawable = teamsLogosArr.get(teamB);
                    break;
                case "timeZone":
                    if (tempValue.equals("Null"))
                        tempValue = null;
                    timeZone = tempValue;
                    break;
                case "liveStreamLink":
                    liveStreamLink = tempValue;
                    break;
                case "backupLiveStreamLink":
                    backupLiveStreamLink = tempValue;
                    break;
                case "competitionInfoTxt":
                    competitionInfoTxt = tempValue;
                    break;
                case "competitionInfoStringImg":
                    competitionInfoStringImg = tempValue;
                    break;
                default:
                    break;
            }
        }
    }
}