package facebookInterview;

import java.util.ArrayList;

import com.restfb.types.Post;

public class Main {


	public static void main(String[] args) {
		InterviewFBClient client = new InterviewFBClient();
		
		System.out.println(client.page.getName());
		
		//client.postUnpublishedDraft("Test unpublished post draft");
		
		//client.postMessage("Test normal Hi Artur");
		long tomorrow = 60L * 60L * 24L;
		long tenMin = 60L * 10L;
		//client.postUnpublishedScheduled("Test scheduled post Hi artur, 10 min from now", tenMin);
		
		//ArrayList<Post> posts = client.getPosts(10);
		
		//print some posts
//		for(Post post : posts){
//			System.out.println(client.postToString(post));
//		}
		
		//client.getRatings(1,5);
		
		
		client.postLink("https://developers.facebook.com/docs/graph-api/reference/v2.11/post");

	}
	

}
