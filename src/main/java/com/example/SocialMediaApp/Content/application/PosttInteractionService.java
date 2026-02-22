package com.example.SocialMediaApp.Content.application;

import com.example.SocialMediaApp.Content.api.dto.CommentRequest;
import com.example.SocialMediaApp.Content.api.dto.CommentResponse;
import com.example.SocialMediaApp.Content.api.dto.LikeResponse;
import com.example.SocialMediaApp.Content.domain.Comment;
import com.example.SocialMediaApp.Content.domain.Post;
import com.example.SocialMediaApp.Content.domain.PostLike;
import com.example.SocialMediaApp.Content.persistence.CommentRepo;
import com.example.SocialMediaApp.Content.persistence.PostLikeRepo;
import com.example.SocialMediaApp.Content.persistence.PostRepo;
import com.example.SocialMediaApp.Content.persistence.StoryRepo;
import com.example.SocialMediaApp.Shared.Exceptions.ActionNotAllowedException;
import com.example.SocialMediaApp.Shared.Exceptions.ContentNotFoundException;
import com.example.SocialMediaApp.Shared.Mappers.Contentmapper;
import com.example.SocialMediaApp.Shared.VisibilityPolicy;
import com.example.SocialMediaApp.User.application.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PosttInteractionService {

    private final PostRepo postRepo;
    private final PostLikeRepo postLikeRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final VisibilityPolicy visibilityPolicy;
    private final CommentRepo commentRepo;
    private final Contentmapper contentmapper;


    public LikeResponse addPostLike(String postId){
        String currentUserId=authenticatedUserService.getcurrentuser();
        Post post=postRepo.findById(postId).orElseThrow(()-> new ContentNotFoundException("Post Not Found"));
        boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,post.getUserId());
        if(!isAllowed){
            throw new ActionNotAllowedException("Action could not be completed Ungranted Permission");
        }
        boolean liked=postLikeRepo.existsByPostIdAndUserId(postId,currentUserId);
        long likeCount;
        if(liked){
            postLikeRepo.deleteByPostIdAndUserId(postId,currentUserId);
            likeCount=postRepo.updatePostLikes(postId,-1);
        }else{
            PostLike postLike=postLikeRepo.save(new PostLike(currentUserId,postId));
            likeCount=postRepo.updatePostLikes(postId,1);
            // handling notifications later
        }
        return new LikeResponse(!liked,likeCount);
    }

    public CommentResponse addPostComment(String postId, CommentRequest commentRequest){
        String currentUserId=authenticatedUserService.getcurrentuser();
        Post post=postRepo.findById(postId).orElseThrow(()-> new ContentNotFoundException("Post Not Found"));
        boolean isAllowed=visibilityPolicy.isAllowed(currentUserId,post.getUserId());
        if(!isAllowed){
            throw new ActionNotAllowedException("Action could not be completed Ungranted Permission");
        }
        Comment comment=commentRepo.save(new Comment(commentRequest.getContent(),currentUserId,postId));
        // handling notification later
        return contentmapper.toCommentResponse(comment);
    }

    public void addCommentLike(String commentId){

    }

    public void addCommentReply(String commendId){

    }

}
