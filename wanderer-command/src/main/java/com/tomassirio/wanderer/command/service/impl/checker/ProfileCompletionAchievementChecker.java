package com.tomassirio.wanderer.command.service.impl.checker;

import com.tomassirio.wanderer.command.repository.UserRepository;
import com.tomassirio.wanderer.commons.domain.AchievementType;
import com.tomassirio.wanderer.commons.domain.User;
import com.tomassirio.wanderer.commons.domain.UserDetails;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Checks the profile-completed achievement (display name and bio both filled in). */
@Component
@RequiredArgsConstructor
public class ProfileCompletionAchievementChecker implements SocialAchievementChecker {

    private final UserRepository userRepository;

    @Override
    public List<AchievementType> getApplicableTypes() {
        return List.of(AchievementType.PROFILE_COMPLETED);
    }

    @Override
    public double computeMetric(UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return 0.0;
        }

        UserDetails details = user.getUserDetails();
        boolean complete =
                details != null
                        && StringUtils.hasText(details.getDisplayName())
                        && StringUtils.hasText(details.getBio());

        return complete ? 1.0 : 0.0;
    }
}
