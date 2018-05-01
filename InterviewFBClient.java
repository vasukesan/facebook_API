package facebookInterview;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonObject;
import com.restfb.json.JsonValue;
import com.restfb.types.FacebookType;
import com.restfb.types.Insight;
import com.restfb.types.Page;
import com.restfb.types.Post;

/**
 * 
 * Create an application for the management of a Facebook Page.
 * The app will be able to create regular posts to a Facebook Page
 * as well as Unpublished Page Posts.
 * The app will be able to list posts from a page (both published and unpublished),
 * and show the number of people who have viewed each post.
 * 
 * @author Varun Amjit Sukesan
 */


public class InterviewFBClient {
	
	FacebookClient fbClient;
	Page page;
	
	public InterviewFBClient(){
		fbClient = new DefaultFacebookClient(Constants.MY_ACCESS_TOKEN,Version.VERSION_2_5);
		page = fbClient.fetchObject("me", Page.class);
		
	}

	
	/**
	 * simple get posts method, gets both published and unpublished
	 * @param limit must be less than 100, or error
	 * @return an array list of post objects
	 */
	ArrayList<Post> getPosts(int limit){
		String lookup = "me/promotable_posts"; //instead of /feed to include unpublished
		
		Connection<Post> myFeed = fbClient.fetchConnection(lookup, Post.class,
				Parameter.with("limit", limit),
				//Parameter.with("include_hidden", true),
				//Parameter.with("include_inline", true),
				//Parameter.with("unpublished_content_type", "DRAFT"),
				//Parameter.with("is_published", false),
				Parameter.with("fields", "is_published,message,id")
				);

		// Iterate over the feed to access the particular pages
		for (List<Post> myFeedPage : myFeed) {

		  // Iterate over the list of contained data 
		  for (Post post : myFeedPage) {
			  //fbClient.deleteObject(post);
		  }
		  
		  return new ArrayList<Post>(myFeedPage);
		}
		return null;
	}
	
	
	/**
	 * helper method for getting number of views from a post
	 * used "post_impressions" based on what I understood from the API doc
	 * @param postID
	 * @return the number of views
	 */
	int getPostViews(String postID){
		Connection<Insight> insightsConnection = fbClient.fetchConnection(postID+"/insights", Insight.class, 
				      Parameter.with("metric", "post_impressions"));

		for (List<Insight> insights: insightsConnection) {
			for (Insight insight : insights) {
				return insight.getValues().get(0).getInt("value", 0);
			}
		}
		return 0;
	}
	
	/**
	 * another helper method for custom printing posts 
	 * @param post
	 * @return
	 */
	String postToString(Post post){
		StringBuilder postString = new StringBuilder(post.getIsPublished() ? "Published" : "Unpublished");
		postString.append(" message: ");
		
		String message = post.getMessage();
		postString.append(message!=null? message.substring(0, Math.min(message.length(), 15)) : "No message");
		
		postString.append(" with " + this.getPostViews(post.getId())+ " views.");
		
		//System.out.println(postString);
		return postString.toString();
	}
	
	
	/**
	 * regular posting
	 * @param message
	 * @return
	 */
	String postMessage(String message){
		FacebookType publishMessageResponse =
		        fbClient.publish("me/feed", FacebookType.class,
		        		Parameter.with("message", message+ Instant.now().getEpochSecond())
		        		);

		return publishMessageResponse.getId();
	}
	
	/**
	 * posting a draft
	 * @param message
	 * @return
	 */
	String postUnpublishedDraft(String message){
	    FacebookType publishEventResponse =
	    		fbClient.publish("me/feed", FacebookType.class,
	    			    Parameter.with("message" + Instant.now().getEpochSecond(), message), 
	    			    Parameter.with("unpublished_content_type", "DRAFT"),
	    				Parameter.with("published", false)
	    				);

	    return publishEventResponse.getId();
	}
	
	/**
	 * posting a scheduled post
	 * @param message
	 * @param secondsFromNow between 10 minutes and 6 months are the limits
	 * @return
	 */
	String postUnpublishedScheduled(String message, long secondsFromNow){
		long publishTime = Instant.now().getEpochSecond() + secondsFromNow;

	    FacebookType publishEventResponse =
	    		fbClient.publish("me/feed", FacebookType.class,
	    			    Parameter.with("message", message + publishTime), 
	    				Parameter.with("published", false),
	    				Parameter.with("scheduled_publish_time", publishTime)
	    				);

	    return publishEventResponse.getId();
	}
	
	/**
	 * special method to assist in replying to review functionality 
	 * @param postID
	 * @param message
	 * @return
	 */
	String comment(String postID, String message){
		FacebookType publishMessageResponse =
		        fbClient.publish(postID+"/comments", FacebookType.class,
		        		Parameter.with("message", message));

		return publishMessageResponse.getId();
	}
	
	/**
	 * access ratings for the page 
	 * @param limit
	 * @param stars
	 */
	void getRatings(int limit, int stars) {
		String lookup = "me/ratings"; //instead of /feed to include unpublished
		JsonObject ratings = fbClient.fetchObject(lookup, JsonObject.class,
				Parameter.with("limit", limit),
				Parameter.with("fields", "open_graph_story")
				);
		JsonValue data = ratings.get("data");

		for(JsonValue review : data.asArray()){
			JsonObject node = review.asObject().get("open_graph_story").asObject();
			int reviewStars = node.get("data").asObject().get("rating").asObject().getInt("value", 5);
			String reviewID = node.get("id").asString();
			if(reviewStars == stars){
				if((reviewID.equals("10156123950779993"))){
					this.comment(reviewID, "We really appreciate your review, Oya! Hope you to see you again soon.");
				}
			}

		}
		
	}
	
	String postLink(String URL){
		FacebookType publishMessageResponse =
		        fbClient.publish("me/feed", FacebookType.class,
		        		Parameter.with("link", URL)
		        		);

		return publishMessageResponse.getId();
	}
}
