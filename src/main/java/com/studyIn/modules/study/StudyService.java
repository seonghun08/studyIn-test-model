package com.studyIn.modules.study;

import com.studyIn.modules.account.Account;
import com.studyIn.modules.location.Location;
import com.studyIn.modules.study.event.StudyCreatedEvent;
import com.studyIn.modules.study.event.StudyUpdateEvent;
import com.studyIn.modules.study.form.DescriptionForm;
import com.studyIn.modules.study.form.PathForm;
import com.studyIn.modules.study.form.TitleForm;
import com.studyIn.modules.tag.Tag;
import com.studyIn.modules.tag.TagService;
import com.studyIn.modules.tag.form.TagForm;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagService tagService;

    public Study createNewStudy(Account account, Study study) {
        study.addManager(account);
        return studyRepository.save(study);
    }

    public Study findPath(String path) {
        Study study = studyRepository.findByPath(path);
        checkExistStudy(study);
        return study;
    }

    public Study VerifyPermissionToUpdateWithAll(Account account, String path) {
        Study study = studyRepository.findByPath(path);
        checkExistStudy(study);
        checkManager(study, account);
        return study;
    }

    public Study VerifyPermissionToUpdateWithTags(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsAndManagersByPath(path);
        checkExistStudy(study);
        checkManager(study, account);
        return study;
    }

    public Study VerifyPermissionToUpdateWithLocations(Account account, String path) {
        Study study = studyRepository.findStudyWithLocationsAndManagersByPath(path);
        checkExistStudy(study);
        checkManager(study, account);
        return study;
    }

    public Study VerifyPermissionToUpdateWithManagers(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkExistStudy(study);
        checkManager(study, account);
        return study;
    }

    public Study findPathWithMembers(String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        checkExistStudy(study);
        return study;
    }

    public Study findPathWithMembersAndManagers(String path) {
        Study study = studyRepository.findStudyWithManagersAndMembersByPath(path);
        checkExistStudy(study);
        return study;
    }

    @Transactional(readOnly = true)
    public Page<Study> searchStudy(String keyword, Pageable pageable) {
        return studyRepository.findByKeyword(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Study> findStudyPageByTagsAndLocations(Set<Tag> tags, Set<Location> locations, Pageable pageable) {
        return studyRepository.findByTagsAndLocations(tags, locations, pageable);
    }

    @Transactional(readOnly = true)
    public StudyList getStudyList_4_ByMemberAndManager(Account account, boolean closed) {
        StudyList studyList = new StudyList();
        studyList.setStudyListManagerOf(
                studyRepository.findFirst4ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, closed)
        );
        studyList.setStudyListMemberOf(
                studyRepository.findFirst4ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, closed)
        );
        return studyList;
    }

    @Transactional(readOnly = true)
    public StudyList getStudyPageByMemberAndManager(Account account, boolean closed, Pageable pageable) {
        StudyList studyList = new StudyList();
        studyList.setStudyPageManagerOf(
                studyRepository.findByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, closed, pageable)
        );
        studyList.setStudyPageMemberOf(
                studyRepository.findByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, closed, pageable)
        );
        return studyList;
    }

    @Transactional(readOnly = true)
    public StudyList getStudyListByMemberAndManager(Account account, boolean closed) {
        StudyList studyList = new StudyList();
        studyList.setStudyListManagerOf(
                studyRepository.findByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, closed)
        );
        studyList.setStudyListMemberOf(
                studyRepository.findByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, closed)
        );
        return studyList;
    }


    /**
     * Study update method
     */
    public void addMember(Account account, Study study) {
        study.addMember(account);
    }
    public void leaveMember(Account account, Study study) {
        study.leaveMember(account);
    }


    /**
     * Validation check method
     */
    private void checkManager(Study study, Account account) {
        if (!study.isManagerBy(account)) {
            throw new AccessDeniedException("?????? ????????? ????????? ??? ?????? ????????? ????????????.");
        }
    }
    private void checkExistStudy(Study study) {
        if (study == null) {
            throw new IllegalArgumentException(study.getPath() + "??? ???????????? ???????????? ???????????? ????????????.");
        }
    }


    /**
     * Study settings update method
     * description, banner, tags, locations, study
     */
    public void updateDescription(DescriptionForm descriptionForm, Study study) {
        modelMapper.map(descriptionForm, study);
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "????????? ????????? ??????????????????."));
    }


    public void updateBanner(Study study, String image) {
        study.setImage(image);
    }
    public void enableBanner(Study study) {
        study.setUseBanner(true);
    }
    public void disableBanner(Study study) {
        study.setUseBanner(false);
    }


    @Transactional(readOnly = true)
    public Set<Tag> findTags(Study study) {
        return studyRepository.findById(study.getId()).orElseThrow().getTags();
    }
    public void addTag(Study study, Tag tag) {
        studyRepository.findById(study.getId()).ifPresent(s -> s.getTags().add(tag));
    }
    public void removeTag(Study study, Tag tag) {
        studyRepository.findById(study.getId()).ifPresent(s -> s.getTags().remove(tag));
    }


    @Transactional(readOnly = true)
    public Set<Location> findLocations(Study study) {
        return studyRepository.findById(study.getId()).orElseThrow().getLocations();
    }
    public void addLocation(Study study, Location location) {
        studyRepository.findById(study.getId()).ifPresent(s -> s.getLocations().add(location));
    }
    public void removeLocation(Study study, Location location) {
        studyRepository.findById(study.getId()).ifPresent(s -> s.getLocations().remove(location));
    }


    public void publish(Study study) {
        study.publish();
        eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }
    public void close(Study study) {
        study.close();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "???????????? ??????????????????."));
    }

    public void startRecruit(Study study) {
        study.startRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "?????? ????????? ???????????????."));
    }
    public void stopRecruit(Study study) {
        study.stopRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "?????? ????????? ???????????????."));
    }

    public void updatePath(Study study, PathForm pathForm) {
        modelMapper.map(pathForm, study);
    }
    public void updateTitle(Study study, TitleForm titleForm) {
        modelMapper.map(titleForm, study);
    }

    public void removeStudy(Study study) {
        if (study.isRemovable()) {
            studyRepository.delete(study);
        } else {
            throw new IllegalArgumentException("???????????? ????????? ??? ????????????.");
        }
    }

    public void generateTestStudies(Account account) {
        for (int i = 0; i < 30; i++) {
            String randomValue = RandomString.make(5);

            Study study = new Study();
            study.setTitle("Test Study :: " + randomValue);
            study.setPath(randomValue);
            study.setShortDescription("????????? :: " + randomValue + " ????????? ?????????.");
            study.publish();
            Study newStudy = this.createNewStudy(account, study);

            TagForm tagForm = new TagForm();
            tagForm.setTagTitle("JPA");

            Tag tag = tagService.findTag(tagForm);
            newStudy.getTags().add(tag);
        }
    }
}
