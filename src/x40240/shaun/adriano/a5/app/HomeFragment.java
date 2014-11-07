package x40240.shaun.adriano.a5.app;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import A5.Shaun.Adriano.R;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {
	TextView prof_name;
	SharedPreferences pref;
	Button prof_img, tweet, signout, post_tweet, refresh;
	ProgressDialog progress;
	Dialog tDialog;
	String tweetText;
	ListView listview;
	ArrayAdapter<String> stringTweetAdapter;
	List<twitter4j.Status> statuses;
	List<String> stringStatuses = new ArrayList<String>();
	ProgressDialog timelineDialog;
	GoHome gohome;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle args) {
		View view = inflater.inflate(R.layout.home_fragment, container,
				false);
		prof_name = (TextView) view.findViewById(R.id.prof_name);
		pref = getActivity().getPreferences(0);
		tweet = (Button) view.findViewById(R.id.tweet);
		signout = (Button) view.findViewById(R.id.signout);
		refresh = (Button) view.findViewById(R.id.refresh);
		listview = (ListView) view.findViewById(R.id.timeline);
		signout.setOnClickListener(new SignOut());
		tweet.setOnClickListener(new Tweet());
		refresh.setOnClickListener(new Refresh());
		prof_name.setText("Welcome, " + pref.getString("NAME", ""));
		new Timeline(true).execute();

		return view;
	}
	
	private class Refresh implements OnClickListener {
		
		@Override
		public void onClick(View arg0) {
			new Timeline(false).execute();
		}
	}

	private class SignOut implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			SharedPreferences.Editor edit = pref.edit();
			edit.putString("ACCESS_TOKEN", "");
			edit.putString("ACCESS_TOKEN_SECRET", "");
			edit.commit();
			Fragment login = new LoginFragment();
			FragmentTransaction ft = getActivity().getFragmentManager()
					.beginTransaction();
			ft.replace(R.id.content_frame, login);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(null);
			ft.commit();

		}

	}

	private class Timeline extends AsyncTask<Void, Void, Void> {
		
		private boolean initial;

		public Timeline(boolean initial) {
			this.initial = initial;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			timelineDialog = new ProgressDialog(getActivity());
			timelineDialog.setMessage("Loading Timeline ...");
			timelineDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			timelineDialog.setIndeterminate(true);
			timelineDialog.show();
		}

		protected Void doInBackground(Void... arg0) {
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
				builder.setOAuthConsumerSecret(pref.getString(
						"CONSUMER_SECRET", ""));

				AccessToken accessToken = new AccessToken(pref.getString(
						"ACCESS_TOKEN", ""), pref.getString(
						"ACCESS_TOKEN_SECRET", ""));
				Twitter twitter = new TwitterFactory(builder.build())
						.getInstance(accessToken);
				User user = twitter.verifyCredentials();

				statuses = twitter.getHomeTimeline();
				stringStatuses.clear();
				
				System.out.println("Showing @" + user.getScreenName()
						+ "'s home timeline.");
				for (twitter4j.Status status : statuses) {
					Log.d("Twitter", "@" + status.getUser().getScreenName()
							+ " - " + status.getText());
					String myTweets = ("@" + status.getUser().getScreenName()
							+ " - " + status.getText());
					stringStatuses.add(myTweets);
				}

			} catch (TwitterException te) {
				te.printStackTrace();
				System.out
						.println("Failed to get timeline: " + te.getMessage());
				// System.exit(-1);
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			timelineDialog.dismiss();
			Toast.makeText(getActivity().getApplicationContext(),
					"Timeline updated", Toast.LENGTH_SHORT).show();
			if (initial == true) {
			stringTweetAdapter = new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, stringStatuses);
			listview.setAdapter(stringTweetAdapter);
			} else stringTweetAdapter.notifyDataSetChanged();

		}

	}

	private class Tweet implements OnClickListener {

		@Override
		public void onClick(View v) {
			tDialog = new Dialog(getActivity());
			tDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			tDialog.setContentView(R.layout.tweet_selection);
			post_tweet = (Button) tDialog.findViewById(R.id.post_tweet);
			post_tweet.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					new PostTweet().execute();
				}
			});

			tDialog.show();

		}
	}

	private class PostTweet extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = new ProgressDialog(getActivity());
			progress.setMessage("Posting tweet ...");
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setIndeterminate(true);
			progress.show();

		}

		protected String doInBackground(String... args) {

			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(pref.getString("CONSUMER_KEY", ""));
			builder.setOAuthConsumerSecret(pref
					.getString("CONSUMER_SECRET", ""));

			AccessToken accessToken = new AccessToken(pref.getString(
					"ACCESS_TOKEN", ""), pref.getString("ACCESS_TOKEN_SECRET",
					""));
			Twitter twitter = new TwitterFactory(builder.build())
					.getInstance(accessToken);
			
			try {
				
				User user = twitter.verifyCredentials();
				twitter4j.Status response = twitter.updateStatus(GoHome.getStatus(user.getScreenName()));
				return response.toString();
			} catch (TwitterException e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(String res) {
			if (res != null) {
				progress.dismiss();
				Toast.makeText(getActivity(), "Tweet Sucessfully Posted",
						Toast.LENGTH_SHORT).show();
				tDialog.dismiss();
			} else {
				progress.dismiss();
				Toast.makeText(getActivity(), "Error while tweeting !",
						Toast.LENGTH_SHORT).show();
				tDialog.dismiss();
			}
			
			new Timeline(false).execute();

		}
	}

}