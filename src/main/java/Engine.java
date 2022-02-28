import entities.Address;
import entities.Employee;
import entities.Project;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

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
                case 2 -> exerciseTwo();
                case 3 -> exerciseThree();
                case 4 -> exerciseFour();
                case 5 -> exerciseFive();
                case 6 -> exerciseSix();
                case 7 -> exerciseSeven();
                case 8 -> exerciseEight();
                case 9 -> exerciseNine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    private void exerciseNine() {
     entityManager.createQuery("FROM Project p ORDER BY p.startDate DESC", Project.class)
                .setMaxResults(10)
                .getResultStream()
                .sorted(Comparator.comparing(Project::getName))
                .forEach(project -> {
                    System.out.println("Project name: " + project.getName());
                    System.out.println("\t Project Description: " + project.getDescription());
                    System.out.println("\t Project Start Date: " + project.getStartDate());
                    System.out.println("\t Project End Date: " + project.getEndDate());
                });

    }

    private void exerciseEight() throws IOException {
        System.out.println("Enter employee id:");
        int e_id = Integer.parseInt(bufferedReader.readLine());
        Employee employee = entityManager.find(Employee.class, e_id);


        System.out.printf("%s %s - %s%n", employee.getFirstName(),
                employee.getLastName(),
                employee.getDepartment().getName());

        employee.getProjects()
                .stream()
                .sorted(Comparator.comparing(Project::getName))
                .forEach(project -> {
                    System.out.println("\t" + project.getName());
                });
    }

    private void exerciseSeven() {
        List<Address> addresses = entityManager
                .createQuery("SELECT a FROM Address a ORDER BY a.employees.size DESC", Address.class)
                .setMaxResults(10)
                .getResultList();
        addresses.forEach(address -> {
            System.out.printf("%s, %s - %d employees%n",
                    address.getText(),
                    address.getTown() == null
                            ? "Unknown" : address.getTown().getName(),
                    address.getEmployees().size());
        });
    }

    private void exerciseSix() throws IOException {
        System.out.println("Enter employee's last name:");
        String input = bufferedReader.readLine();

        Employee employee = entityManager.createQuery("SELECT e FROM Employee e " +
                        "WHERE e.lastName = :l_name", Employee.class)
                .setParameter("l_name", input)
                .getSingleResult();

        //С метода се добавя адреса в базата данни
        Address address = createAddress("Vitoshka 15");

        //Добавяме адреса към въпросното емплои
        entityManager.getTransaction().begin();
        employee.setAddress(address);
        entityManager.getTransaction().commit();


    }

    private Address createAddress(String addressText) {
        //Създаваме обект от клас адрес
        Address address = new Address();
        //Добавяме му адреса, който ни е даден
        address.setText(addressText);
        //Запазваме го в базата
        entityManager.getTransaction().begin();
        entityManager.persist(address);
        entityManager.getTransaction().commit();
        return address;
    }

    private void exerciseFive() {
        List<Employee> employeeList = entityManager
                .createQuery("SELECT e FROM Employee e " +
                        "WHERE e.department.name = :d_name " +
                        "ORDER BY e.salary, e.id", Employee.class)
                .setParameter("d_name", "Research and Development")
                .getResultList();

            employeeList.forEach(employee -> {
                System.out.printf("%s %s from %s - $%.2f%n",
                        employee.getFirstName(),
                        employee.getLastName(),
                        //Обекта Емплои в Джава има достъп и до останалата информация
                        //от навързаните ентитита (в случая департмент)
                        employee.getDepartment().getName(),
                        employee.getSalary());
            });
    }

    private void exerciseFour() throws IOException {
        System.out.println("Enter minimum salary for the list:");
        //Тъй като в ентитито салари е BigDecimal, трябва да подадем
        //същият тип като параметър
        BigDecimal sal = new BigDecimal(Long.parseLong(bufferedReader.readLine()));
        entityManager.createQuery("SELECT e from Employee e WHERE e.salary > :e_salary", Employee.class)
                .setParameter("e_salary", sal)
                //с getResultStream създаваме стрийм с върнатите резултати
                //от заявката
                .getResultStream()
                //Взимаме от всяко емплои само първото име
                .map(Employee::getFirstName)
                //Принтираме
                .forEach(System.out::println);

    }

    private void exerciseThree() throws IOException {
        System.out.println("Enter Full name:");
        String[] fullName = bufferedReader.readLine().split("\\s+");
        String firstName = fullName[0];
        String lastName = fullName[1];


        //Ако не се зададат променливите, ще гръмне.
        //В края - х.class указва какъв ще е типа данни, които ще се връщат
        Long singleResult = entityManager.createQuery("SELECT count(e) from Employee e  WHERE e.firstName = :f_name AND e.lastName = :l_name", Long.class)
                .setParameter("f_name", firstName)
                .setParameter("l_name", lastName)
                .getSingleResult();

        System.out.println(singleResult == 0
                ? "No" : "Yes");
    }

    private void exerciseTwo() {
        entityManager.getTransaction().begin();
        Query query = entityManager.createQuery("UPDATE Town t SET t.name = UPPER(t.name) " +
                "WHERE LENGTH(t.name) <= 5");
        System.out.println(query.executeUpdate());

        entityManager.getTransaction().commit();
    }
}
