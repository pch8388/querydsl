package data.jpa.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import data.jpa.querydsl.entity.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static data.jpa.querydsl.entity.QMember.member;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(em.find(Member.class, id));
    }

    public List<Member> findAll() {
        return jpaQueryFactory
            .selectFrom(member)
            .fetch();
    }

    public List<Member> findByUsername(String username) {
        return jpaQueryFactory
            .selectFrom(member)
            .where(member.username.eq(username))
            .fetch();
    }
}
