package com.anurag.ai.repository;

import com.anurag.ai.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserIdOrderByCompletedAscIdAsc(Long userId);
    List<Task> findByUserIdOrderByIdAsc(Long userId);
    void deleteByUserId(Long userId);

    /** Rows of [userId, totalTasks, completedTasks]. */
    @Query("""
        select t.userId, count(t), sum(case when t.completed = true then 1 else 0 end)
        from Task t where t.userId in :ids group by t.userId
        """)
    List<Object[]> stats(@Param("ids") Collection<Long> ids);
}
