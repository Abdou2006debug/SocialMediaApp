package com.example.whatsappclone.Profile.application;

import com.example.whatsappclone.Profile.api.dto.profileSummary;
import com.example.whatsappclone.Profile.application.cache.ProfileCacheManager;
import com.example.whatsappclone.Profile.domain.Profile;
import com.example.whatsappclone.Profile.domain.cache.ProfileInfo;
import com.example.whatsappclone.Profile.persistence.ProfileRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProfileSummaryService {
    private final ProfileCacheManager profileCacheManager;
    private final ProfileRepo profileRepo;

    public List<profileSummary> buildProfileSummaries(List<String> usersIds){
        List<profileSummary> profileSummaries=usersIds.stream().map(profileSummary::new).toList();
        Map<String, profileSummary> summaryMap = profileSummaries.stream()
                .collect(Collectors.toMap(profileSummary::getUserId, Function.identity()));
        // fetching from cache first
        usersIds.forEach(userId->{
            Optional<ProfileInfo> profileInfo= profileCacheManager.getProfileInfo(userId);
            if(profileInfo.isPresent()){
                profileSummary profileSummary=summaryMap.get(userId);
                ProfileInfo profileInfo1 = profileInfo.get();
                profileSummary.setAvatarurl(profileInfo1.getAvatarurl());
                profileSummary.setUsername(profileInfo1.getUsername());
            }
        });
        // filtering the non fetched profile summaries to fetch them from db and cache them also
        usersIds=summaryMap.entrySet().stream().
                filter(e->e.getValue().getUsername()==null).map(Map.Entry::getKey).toList();
        List<Profile> profiles= profileRepo.findByUserIdIn(usersIds);
        for(Profile profile:profiles){
            profileSummary profileSummary=summaryMap.get(profile.getUserId());
            profileSummary.setUsername(profile.getUsername());
            profileSummary.setAvatarurl(profile.getPublicavatarurl());
            profileCacheManager.cacheProfileInfo(profile);
        }

        return profileSummaries;
    }

}
