package data.jpa.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import data.jpa.querydsl.dto.MemberSearchCondition;
import data.jpa.querydsl.dto.MemberTeamDto;
import data.jpa.querydsl.dto.QMemberTeamDto;
import data.jpa.querydsl.entity.Member;
import data.jpa.querydsl.entity.QTeam;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static data.jpa.querydsl.entity.QMember.member;
import static data.jpa.querydsl.entity.QTeam.*;
import static org.springframework.util.StringUtils.*;

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

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return jpaQueryFactory
            .select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")))
            .from(member)
            .leftJoin(member.team, team)
            .where(builder)
            .fetch();
    }
}
