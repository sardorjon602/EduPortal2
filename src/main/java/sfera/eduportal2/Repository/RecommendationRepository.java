package sfera.eduportal2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sfera.eduportal2.entity.Recommendation;
import sfera.eduportal2.entity.Users;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    // Foydalanuvchi bo'yicha barcha tavsiyalarni olish
    List<Recommendation> findAllByUsers(Users users);

    // Foydalanuvchi bo'yicha eng so'nggi tavsiyani olish
    Optional<Recommendation> findTopByUsersOrderByCreatedAtDesc(Users users);

    // Foydalanuvchi uchun tavsiya mavjudligini tekshirish
    boolean existsByUsersId(Long userId);
}