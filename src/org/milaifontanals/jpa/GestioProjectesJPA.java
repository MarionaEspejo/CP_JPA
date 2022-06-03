/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.milaifontanals.jpa;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.mf.persistence.GestioProjectesException;
import org.mf.persistence.IGestioProjectes;
import org.milaifontanals.model.Entrada;
import org.milaifontanals.model.Estat;
import org.milaifontanals.model.Projecte;
import org.milaifontanals.model.ProjecteUsuariRol;
import org.milaifontanals.model.Rol;
import org.milaifontanals.model.Tasca;
import org.milaifontanals.model.Usuari;
import org.milaifontanals.model.UsuariToken;

public class GestioProjectesJPA implements IGestioProjectes {

    private EntityManager em;
    private HashMap<Integer, Usuari> hmUsuaris = new HashMap();

    public GestioProjectesJPA() throws GestioProjectesException {
        this("GestioProjectesJPA.properties");
    }

    public GestioProjectesJPA(String nomFitxerPropietats) throws GestioProjectesException {
        if (nomFitxerPropietats == null) {
            nomFitxerPropietats = "GestioProjectesJPA.properties";
        }
        Properties props = new Properties();
        try {
            props.load(new FileReader(nomFitxerPropietats));
        } catch (FileNotFoundException ex) {
            throw new GestioProjectesException("No es troba fitxer de propietats", ex);
        } catch (IOException ex) {
            throw new GestioProjectesException("Error en carregar fitxer de propietats", ex);
        }

        String up = props.getProperty("up");
        if (up == null) {
            throw new GestioProjectesException("Fitxer de propietats no conté propietat obligatòria <up>");
        }
        props.remove(up);

        EntityManagerFactory emf = null;
        try {
            emf = Persistence.createEntityManagerFactory(up, props);
//            System.out.println("EMF creat");
            em = emf.createEntityManager();
//            System.out.println("EM creat");
        } catch (Exception ex) {
            if (emf != null) {
                emf.close();
            }
            throw new GestioProjectesException("Error en crear EntityManagerFactory o EntityManager", ex);
        }

    }

    @Override
    public void closeCapa() throws GestioProjectesException {
        if (em != null) {
            EntityManagerFactory emf = null;
            try {
                emf = em.getEntityManagerFactory();
                em.close();
            } catch (Exception ex) {
                throw new GestioProjectesException("Error en tancar la connexió", ex);
            } finally {
                em = null;
                if (emf != null) {
                    emf.close();
                }
            }
        }
    }

    @Override
    public void commit() throws GestioProjectesException {
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            throw new GestioProjectesException("Error en fer commit.", ex);
        }
    }

    @Override
    public void rollback() throws GestioProjectesException {
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.getTransaction().rollback();
        } catch (Exception ex) {
            throw new GestioProjectesException("Error en fer rollback.", ex);
        }
    }

    @Override
    public UsuariToken Login(String user, String password) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closeTransaction(char typeClose) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Usuari> getLlistaUsuaris() throws GestioProjectesException {
        Query query = em.createQuery("select u from Usuari u", Usuari.class);
        List<Usuari> usuaris = query.getResultList();
        return usuaris;
    }

    @Override
    public List<Projecte> getLlistaProjectes(Usuari usuari) throws GestioProjectesException {
        Query query = em.createQuery("select pur.projecte from ProjecteUsuariRol pur where pur.usuari = :pUsuari", Projecte.class);
        query.setParameter("pUsuari", usuari);
        List<Projecte> projectes = query.getResultList();
        return projectes;
    }

    @Override
    public List<Projecte> getLlistaProjectesNoAssignats(Usuari usuari) throws GestioProjectesException {
        Query query = em.createQuery("select distinct p from ProjecteUsuariRol pur right join pur.projecte p where pur.usuari is null or p not in (Select p from ProjecteUsuariRol pur join pur.projecte p where pur.usuari =: pUsuari)", Projecte.class);
        query.setParameter("pUsuari", usuari);
        List<Projecte> projectes = query.getResultList();
        return projectes;
    }

    @Override
    public Usuari getUsuari(int id) throws GestioProjectesException {
        return em.find(Usuari.class, (int) id);
    }

    @Override
    public Projecte getProjecte(int id) throws GestioProjectesException {
        return em.find(Projecte.class, (int) id);
    }

    @Override
    public Rol getRol(int id) throws GestioProjectesException {
        return em.find(Rol.class, (int) id);
    }

    @Override
    public Rol getRolUsu(Projecte idProj, Usuari idUsu) throws GestioProjectesException {
        Query query = em.createQuery("select pur.rol "
                + "from ProjecteUsuariRol pur "
                + "where pur.usuari =: idUsu and pur.projecte =: idProj ", Rol.class);
        query.setParameter("idProj", idProj);
        query.setParameter("idUsu", idUsu);
        Rol rol = (Rol) query.getSingleResult();
        return rol;
    }

    @Override
    public int deleteUsuari(int id) throws GestioProjectesException {
        Usuari usu = em.find(Usuari.class, (int) id);
        em.getTransaction().begin();
        em.merge(usu);
        em.remove(usu);
        em.getTransaction().commit();
        return 0;
    }

    @Override
    public int insertUsuari(Usuari usu) throws GestioProjectesException {
        em.getTransaction().begin();
        em.persist(usu);
        em.getTransaction().commit();
        return 0;
    }

    @Override
    public int updateUsuari(Usuari usu) throws GestioProjectesException {
        em.getTransaction().begin();
        Usuari usuUpdate = em.find(Usuari.class, (int) usu.getID());
        usuUpdate.setNom(usu.getNom());
        usuUpdate.setCognom1(usu.getCognom1());
        usuUpdate.setCognom2(usu.getCognom2());
        usuUpdate.setDataNaix(usu.getDataNaix());
        usuUpdate.setLogin(usu.getLogin());
        usuUpdate.setHashPasswd(usu.getPasswdHash());
        em.merge(usuUpdate);
        em.persist(usuUpdate);
        em.getTransaction().commit();
        return 0;
    }

    @Override
    public void assignarProjecte(Usuari usu, Projecte proj, Rol rol) throws GestioProjectesException {
        em.getTransaction().begin();
        ProjecteUsuariRol pur = new ProjecteUsuariRol(proj, usu, rol);
        em.persist(pur);
        em.getTransaction().commit();
    }

    @Override
    public void desassignarProjecte(Usuari usu, Projecte proj, Rol rol) throws GestioProjectesException {
        em.getTransaction().begin();
        ProjecteUsuariRol pur = new ProjecteUsuariRol(proj, usu, rol);
        em.remove(em.merge(pur));
        em.getTransaction().commit();
    }

    @Override
    public int ultimID() throws GestioProjectesException {
        Query query = em.createQuery("select max(u.id) "
                + "from Usuari u ");
        int ultimID = (int) query.getSingleResult();
        return ultimID;
    }

    @Override
    public List<Tasca> GetTasquesAssignades(Usuari usu) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getID(String login, String pwd) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getProjIDTasca(int id) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Projecte> getProjectes() throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Tasca> getTasquesIDProj(int idProj) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Entrada> getEntradaIDTasca(int idTask) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Projecte> getProjecteFiltreNom(String nom) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Estat> getEstats() throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Projecte> getLlistaProjectesTascaEstat(String nomEstat) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int NovaEntrada(Entrada newEntrada, int idTask) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getNumeroEntrada(int idTaca) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Estat getEstat(String nomEstat) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Projecte> getProjecteFiltreTextTasca(String testTask) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Tasca> getTasquesIDProjTots(int idProj) throws GestioProjectesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
