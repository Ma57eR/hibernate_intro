import entities.Employee;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Engine implements Runnable {

    private final EntityManager entityManager;

    private BufferedReader bufferedReader;

    public Engine(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }


    @Override
    public void run() {
        System.out.println("Select exercise number:");
        int exNum;
        try {
            exNum = Integer.parseInt(bufferedReader.readLine());

            switch (exNum) {
                case 2: exerciseTwo();
                break;
                case 3: exerciseThree();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    private void exerciseThree() throws IOException {
        System.out.println("Enter Full name:");
        String[] fullName = bufferedReader.readLine().split("\\s+");
        String firstName = fullName[0];
        String lastName = fullName[1];

        Employee employee = entityManager.createQuery("select e from employees e ", Employee.class)
                .getSingleResult();


    }

    private void exerciseTwo() {
        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery("UPDATE Town t SET t.name = UPPER(t.name) " +
                "WHERE LENGTH(t.name) <= 5");
        System.out.println(query.executeUpdate());

        entityManager.getTransaction().commit();
    }
}
