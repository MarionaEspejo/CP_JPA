/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.milaifontanals.jpa;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.mf.persistence.GestioProjectesException;
import org.mf.persistence.IGestioProjectes;

/**
 *
 * @author anna9
 */
public class GestioProjectesJPA implements IGestioProjectes {
    private EntityManager em;

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
    public List<Object> Login(String user, String password) throws GestioProjectesException {
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
    
    
}
