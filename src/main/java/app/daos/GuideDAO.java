package app.daos;

import app.entities.Guide;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class GuideDAO implements IDAO<Guide,Integer> {
    private final EntityManagerFactory emf;

    public GuideDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Guide create(Guide guide) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(guide);
            em.getTransaction().commit();
            return guide;
        }catch(Exception ex){
            throw new ApiException(500, "Error Creating Guide: " + ex.getMessage());
        }
    }

    @Override
    public Guide getById(int id) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Guide guide = em.find(Guide.class, id);
            em.getTransaction().commit();
            return guide;
        }catch(Exception ex){
            throw new ApiException(500, "Error Getting Guide: " + ex.getMessage());
        }
    }

    @Override
    public List<Guide> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("select g from Guide g",
                    Guide.class)
                    .getResultList();
        }
    }

    @Override
    public Guide update(int id, Guide guide) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();

            Guide existingGuide = em.find(Guide.class, id);
            if(existingGuide ==  null){
                throw new ApiException(500, "Error Updating Guide");
            }
            //Ensure correct id
            existingGuide.setId(id);

            Guide updatedGuide = em.merge(guide);
            em.getTransaction().commit();
            return updatedGuide;
        }catch (Exception ex){
            throw new ApiException(500, "Error Updating Guide: " + ex.getMessage());
        }
    }
    /*
    @Override
    public Guide update(int id, Guide guide) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Guide existing = em.find(Guide.class, id);
            if (existing == null)
                throw new ApiException(404, "Guide not found with id: " + id);
            existing.setName(guide.getName());
            existing.setEmail(guide.getEmail());
            existing.setPhoneNumber(guide.getPhoneNumber());
            existing.setYearsOfExperience(guide.getYearsOfExperience());
            em.merge(existing);
            em.getTransaction().commit();
            return existing;
        }
    }
     */

    @Override
    public boolean deleteById(int id) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Guide existing = em.find(Guide.class, id);
            if (existing == null){
                throw new ApiException(500, "Error Deleting Guide");
            }
            em.remove(existing);
            em.getTransaction().commit();
            return true;
        }catch (Exception ex){
            throw new ApiException(500, "Error Deleting Guide: " + ex.getMessage());
        }
    }
}
