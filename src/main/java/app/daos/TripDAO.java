package app.daos;

import app.entities.Trip;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class TripDAO implements IDAO<Trip,Integer> {
    private final EntityManagerFactory emf;

    public TripDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Trip create(Trip trip) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(trip);
            em.getTransaction().commit();
            return trip;
        }catch (Exception ex){
            throw new ApiException(500, "Error Creating Trip: " + ex.getMessage());
        }
    }

    @Override
    public Trip getById(int id) {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("SELECT DISTINCT t FROM Trip t LEFT JOIN FETCH t.guide WHERE t.id = :id ",
                    Trip.class)
                    .setParameter("id", id)
                    .getSingleResult();
        }catch (Exception ex){
            throw new ApiException(500, "Error Getting Trip: " + ex.getMessage());
        }
    }

    @Override
    public List<Trip> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            return em.createQuery("SELECT DISTINCT t from Trip t LEFT JOIN FETCH t.guide ORDER BY t.id ASC",
                    Trip.class)
                    .getResultList();
        }catch (Exception ex){
            throw new ApiException(500, "Error Getting All Trips: " + ex.getMessage());
        }
    }

    @Override
    public Trip update(int id, Trip trip) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();

            Trip existing = em.find(Trip.class, id);
            if(existing == null){
                throw new ApiException(404, "Trip Not Found: " + id);
            }
            //Ensure correct id
            trip.setId(id);

            Trip updated = em.merge(trip);
            em.getTransaction().commit();
            return updated;
        }catch (Exception ex){
            throw new ApiException(500, "Error Updating Trip: " + ex.getMessage());
        }
    }
    /*
    @Override
    public Trip update(int id, Trip trip) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Trip existing = em.find(Trip.class, id);
            if (existing == null)
                throw new ApiException(404, "Trip not found with id: " + id);

            existing.setName(trip.getName());
            existing.setStartTime(trip.getStartTime());
            existing.setEndTime(trip.getEndTime());
            existing.setLatitude(trip.getLatitude());
            existing.setLongitude(trip.getLongitude());
            existing.setPrice(trip.getPrice());
            existing.setCategory(trip.getCategory());
            existing.setGuide(trip.getGuide());

            em.merge(existing);
            em.getTransaction().commit();
            return existing;
        } catch (Exception e) {
            throw new ApiException(500, "Error updating trip: " + e.getMessage());
        }
    }
     */

    @Override
    public boolean deleteById(int id) {
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            Trip existing = em.find(Trip.class, id);
            if(existing == null){
                throw new ApiException(404, "Trip Not Found: " + id);
            }
            em.remove(existing);
            em.getTransaction().commit();
            return true;
        }catch (Exception ex){
            throw new ApiException(500, "Error Deleting Trip: " + ex.getMessage());
        }
    }
}
