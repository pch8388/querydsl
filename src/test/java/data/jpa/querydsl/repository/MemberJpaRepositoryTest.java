package data.jpa.querydsl.repository;

import data.jpa.querydsl.dto.MemberSearchCondition;
import data.jpa.querydsl.dto.MemberTeamDto;
import data.jpa.querydsl.entity.Member;
import data.jpa.querydsl.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;


    @Test
    public void basicQuerydslTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        final Optional<Member> findMember = memberJpaRepository.findById(member.getId());
        assertThat(findMember.get()).isEqualTo(member);

        final List<Member> allMember = memberJpaRepository.findAll();
        assertThat(allMember).containsExactly(member);

        final List<Member> findUsername = memberJpaRepository.findByUsername("member1");
        assertThat(findUsername).containsExactly(member);
    }

    @Test
    public void searchTest() {
        Team team1 = new Team("team1");
        Team team2 = new Team("team2");
        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("member1", 10, team1);
        Member member2 = new Member("member2", 20, team1);
        Member member3 = new Member("member3", 30, team2);
        Member member4 = new Member("member4", 40, team2);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition1 = new MemberSearchCondition();
        condition1.setAgeGoe(35);
        condition1.setAgeLoe(40);
        condition1.setTeamName("team2");

        final List<MemberTeamDto> result =
            memberJpaRepository.searchByBuilder(condition1);

        assertThat(result).extracting("username").containsExactly("member4");

        MemberSearchCondition condition2 = new MemberSearchCondition();
        condition2.setTeamName("team1");

        final List<MemberTeamDto> result2 =
            memberJpaRepository.searchByBuilder(condition2);

        assertThat(result2).extracting("username").containsExactly("member1", "member2");
    }

    @Test
    public void search() {
        Team team1 = new Team("team1");
        Team team2 = new Team("team2");
        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("member1", 10, team1);
        Member member2 = new Member("member2", 20, team1);
        Member member3 = new Member("member3", 30, team2);
        Member member4 = new Member("member4", 40, team2);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition1 = new MemberSearchCondition();
        condition1.setAgeGoe(35);
        condition1.setAgeLoe(40);
        condition1.setTeamName("team2");

        final List<MemberTeamDto> result =
            memberJpaRepository.search(condition1);

        assertThat(result).extracting("username").containsExactly("member4");

        MemberSearchCondition condition2 = new MemberSearchCondition();
        condition2.setTeamName("team1");

        final List<MemberTeamDto> result2 =
            memberJpaRepository.search(condition2);

        assertThat(result2).extracting("username").containsExactly("member1", "member2");
    }
}