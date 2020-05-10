package data.jpa.querydsl.repository;

import data.jpa.querydsl.entity.Member;
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
}