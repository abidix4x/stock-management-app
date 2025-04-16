package com.example.gestion.dao;

import com.example.gestion.DatabaseService;
import com.example.gestion.model.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public static Service getById(int id) {
        Service service = null;
        String query = "SELECT * FROM services WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                service = extractServiceFromResultSet(rs);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du service: " + e.getMessage());
            e.printStackTrace();
        }

        return service;
    }

    public static List<Service> getAll() {
        List<Service> services = new ArrayList<>();
        String query = "SELECT * FROM services";

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Service service = extractServiceFromResultSet(rs);
                services.add(service);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des services: " + e.getMessage());
            e.printStackTrace();
        }

        return services;
    }

    private static Service extractServiceFromResultSet(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setId(rs.getInt("id"));
        service.setNom(rs.getString("nom"));
        service.setResponsable(rs.getString("responsable"));
        service.setTelephone(rs.getString("telephone"));
        service.setEmail(rs.getString("email"));
        return service;
    }

    public static List<Service> searchServices(String keyword) {
        List<Service> services = new ArrayList<>();
        String query = "SELECT * FROM services WHERE nom LIKE ? OR responsable LIKE ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Service service = extractServiceFromResultSet(rs);
                services.add(service);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des services: " + e.getMessage());
            e.printStackTrace();
        }

        return services;
    }

    public static boolean insert(Service service) {
        String query = "INSERT INTO services (nom, responsable, telephone, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, service.getNom());
            pstmt.setString(2, service.getResponsable());
            pstmt.setString(3, service.getTelephone());
            pstmt.setString(4, service.getEmail());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    service.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean update(Service service) {
        String query = "UPDATE services SET nom = ?, responsable = ?, telephone = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, service.getNom());
            pstmt.setString(2, service.getResponsable());
            pstmt.setString(3, service.getTelephone());
            pstmt.setString(4, service.getEmail());
            pstmt.setInt(5, service.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(int id) {
        String query = "DELETE FROM services WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du service: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
