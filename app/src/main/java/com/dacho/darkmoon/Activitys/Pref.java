    package com.dacho.darkmoon.activitys;

    import android.os.AsyncTask;
    import android.os.Bundle;
    import android.os.PersistableBundle;
    import android.preference.ListPreference;
    import android.preference.PreferenceActivity;
    import android.preference.PreferenceCategory;
    import android.util.Log;

    import com.dacho.darkmoon.R;
    import com.dacho.darkmoon.layoutFragment.Preference;

    import java.util.ArrayList;

    import twitter4j.Category;
    import twitter4j.ResponseList;
    import twitter4j.Twitter;
    import twitter4j.UserList;

    /**
     * Created by admin on 2015/07/17.
     */
    public class Pref extends PreferenceActivity{

        twitter4j.Twitter twitter;
        PreferenceCategory listPreferenceCategory;
        ListPreference list_preference;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //getFragmentManager().beginTransaction().replace(android.R.id.content, new Preference()).commit();
            addPreferencesFromResource(R.xml.preference);
                twitter = MainActivity.getTwitter();
                listPreferenceCategory = (PreferenceCategory) findPreference("list_category");
                list_preference = (ListPreference)listPreferenceCategory.findPreference("list_preference");
                Log.d("list",""+listPreferenceCategory);
                if (list_preference != null) {
                    new ListAsyncTask().execute();
                }
            }

        public class ListAsyncTask extends AsyncTask<String,String,String> {

            @Override
            protected String doInBackground(String... strings) {
                try{
                    ResponseList<UserList> userLists = twitter.getUserLists(twitter.getId());
                    CharSequence entries[] = new String[userLists.size()];
                    CharSequence entryValues[] = new String[userLists.size()];
                    int i = 0;
                    for (UserList list : userLists) {
                        entries[i] = list.getName();
                        entryValues[i] = Long.toString(list.getId());
                        Log.d("name", "" + entries[i]);
                        i++;
                    }
                    list_preference.setEntries(entries);
                    list_preference.setEntryValues(entryValues);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }

        }
