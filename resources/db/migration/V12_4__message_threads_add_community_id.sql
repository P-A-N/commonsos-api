ALTER TABLE message_threads ADD COLUMN community_id BIGINT;

update message_threads mt
set community_id = (
    select min(community_id) from (
        select cu1.community_id from message_thread_parties mtp1
        inner join community_users cu1
        on cu1.user_id = mtp1.user_id
        where mtp1.message_thread_id = mt.id
        group by cu1.community_id
        having count(*) = (
            select max(cnt) from (
                select cu2.community_id, count(*) as cnt from message_thread_parties mtp2
                inner join community_users cu2
                on cu2.user_id = mtp2.user_id
                where mtp2.message_thread_id = mt.id
                group by cu2.community_id
            ) tmp2
        )
    ) tmp1
);
