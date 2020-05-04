package data.jpa.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import data.jpa.querydsl.dto.MemberDto;
import data.jpa.querydsl.entity.Member;
import data.jpa.querydsl.entity.QMember;
import data.jpa.querydsl.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static data.jpa.querydsl.entity.QMember.member;
import static data.jpa.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QueryBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    public void setUp() {
        jpaQueryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    public void jpql() {
        String query =
            "select m from Member m " +
            "where m.username = :username";
        Member findMember = em.createQuery(query, Member.class)
            .setParameter("username", "member1")
            .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void querydsl() {
        Member findMember = jpaQueryFactory
            .select(member)
            .from(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = jpaQueryFactory
            .selectFrom(member)
            .where(member.username.eq("member1")
                .and(member.age.between(10, 30)))
            .fetchOne();

        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    public void searchAndParam() {
        Member findMember = jpaQueryFactory
            .selectFrom(member)
            .where(
                member.team.name.eq("team1"),
                member.age.goe(10)
            ).fetchFirst();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void order() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> members = jpaQueryFactory
            .selectFrom(member)
            .where(member.age.eq(100))
            .orderBy(member.age.desc(), member.username.asc().nullsLast())
            .fetch();

        Member member5 = members.get(0);
        Member member6 = members.get(1);
        Member memberNull = members.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging() {
        QueryResults<Member> results = jpaQueryFactory
            .selectFrom(member)
            .where(member.age.goe(0))
            .offset(0)
            .limit(2)
            .fetchResults();

        assertThat(results.getTotal()).isEqualTo(4);
        assertThat(results.getResults().size()).isEqualTo(2);
        assertThat(results.getLimit()).isEqualTo(2);
        assertThat(results.getOffset()).isEqualTo(0);
    }

    @Test
    public void aggregation() {
        Tuple tuple = jpaQueryFactory
            .select(
                member.count(),
                member.age.sum(),
                member.age.avg(),
                member.age.max(),
                member.age.min())
            .from(member)
            .fetchOne();

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    public void join() {
        final List<Member> members = jpaQueryFactory
            .selectFrom(member)
            .join(member.team, team)
            .where(team.name.eq("team1"))
            .fetch();

        assertThat(members)
            .extracting("username")
            .containsExactly("member1", "member2");
    }

    @Test
    public void theta_join() {
        em.persist(new Member("team1"));
        em.persist(new Member("team2"));
        em.persist(new Member("team3"));

        final List<Member> members = jpaQueryFactory
            .select(member)
            .from(member, team)
            .where(member.username.eq(team.name))
            .fetch();

        assertThat(members)
            .extracting("username")
            .containsExactly("team1", "team2");
    }

    @Test
    public void left_outer_join() {
        final List<Tuple> tuples = jpaQueryFactory
            .select(member, team)
            .from(member)
            .leftJoin(member.team, team)
            .on(member.team.name.eq("team1"))
            .fetch();

        for (Tuple tuple : tuples) {
            System.out.println("tuple : " + tuple);
        }
    }

    @Test
    public void join_on_no_relation() {
        em.persist(new Member("team1"));
        em.persist(new Member("team2"));
        em.persist(new Member("team3"));

        final List<Tuple> members = jpaQueryFactory
            .select(member, team)
            .from(member)
            .leftJoin(team)
            .on(member.username.eq(team.name))
            .fetch();

        for (Tuple tuple : members) {
            System.out.println("member : " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetch_join() {
        em.flush();
        em.clear();

        final Member findMember = jpaQueryFactory
            .select(QMember.member)
            .from(QMember.member)
            .join(QMember.member.team, team).fetchJoin()
            .where(QMember.member.username.eq("member1"))
            .fetchOne();

        final boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).isTrue();
    }

    @Test
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");

        final Integer maxAge = jpaQueryFactory
            .select(member.age)
            .from(member)
            .where(member.age.eq(
                select(memberSub.age.max())
                    .from(memberSub)))
            .fetchOne();

        assertThat(maxAge).isEqualTo(40);
    }

    @Test
    public void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");

        final List<Member> members = jpaQueryFactory
            .select(member)
            .from(member)
            .where(member.age.goe(
                select(memberSub.age.avg())
                    .from(memberSub)))
            .fetch();

        assertThat(members).extracting("age")
            .containsExactly(30, 40);
    }

    @Test
    public void subQueryIn() {
        QMember memberSub = new QMember("memberSub");

        final List<Member> members = jpaQueryFactory
            .select(member)
            .from(member)
            .where(member.age.in(
                select(memberSub.age)
                    .from(memberSub)
                    .where(memberSub.age.gt(10))))
            .fetch();

        assertThat(members).extracting("age")
            .containsExactly(20, 30, 40);
    }

    @Test
    public void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");

        final List<Tuple> tuples = jpaQueryFactory
            .select(member.username,
                    select(memberSub.age.avg())
                        .from(memberSub))
            .from(member)
            .fetch();

        tuples.forEach(System.out::println);
    }

    @Test
    public void basicCase() {
        final List<String> fetch = jpaQueryFactory
            .select(member.age
                .when(10).then("열살")
                .when(20).then("스무살")
                .otherwise("기타"))
            .from(member)
            .fetch();

        for (String s : fetch) {
            System.out.println(s);
        }
    }

    @Test
    public void caseBuilder() {
        final List<String> fetch = jpaQueryFactory
            .select(new CaseBuilder()
                .when(member.age.between(0, 20)).then("0 ~ 20 살")
                .when(member.age.between(21, 30)).then("20 ~ 30 살")
                .otherwise("기타"))
            .from(member)
            .fetch();

        for (String s : fetch) {
            System.out.println(s);
        }
    }

    @Test
    public void constant() {
        final Tuple tuple = jpaQueryFactory
            .select(member.username, Expressions.constant("A"))
            .from(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        System.out.println(tuple);
    }

    @Test
    public void concat() {
        final String s = jpaQueryFactory
            .select(member.username.concat("_").concat(member.age.stringValue()))
            .from(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        assertThat(s).isEqualTo("member1_10");
    }

    @Test
    public void projectionJpql() {
        final List<MemberDto> results = em.createQuery("" +
            "select new data.jpa.querydsl.dto.MemberDto(m.username, m.age) " +
            "from Member m", MemberDto.class)
            .getResultList();

        for (MemberDto result : results) {
            System.out.println("member : " + result);
        }
    }

    @Test
    public void projectionQuerydsl() {
        final List<MemberDto> beans = jpaQueryFactory
            .select(Projections.bean(MemberDto.class,
                member.username, member.age))
            .from(member)
            .fetch();

        for (MemberDto bean : beans) {
            System.out.println("bean : " + bean);
        }

        final List<MemberDto> fields = jpaQueryFactory
            .select(Projections.fields(MemberDto.class,
                member.username, member.age))
            .from(member)
            .fetch();

        for (MemberDto field : fields) {
            System.out.println("field : " + field);
        }

        final List<MemberDto> constructors = jpaQueryFactory
            .select(Projections.constructor(MemberDto.class,
                member.username, member.age))
            .from(member)
            .fetch();

        for (MemberDto constructor : constructors) {
            System.out.println("constructor : " + constructor);
        }
    }
}
