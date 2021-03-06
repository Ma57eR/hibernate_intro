import entities.Address;
import entities.Employee;
import entities.Project;
import entities.Town;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class Engine implements Runnable {

    private final EntityManager entityManager;

    private final BufferedReader bufferedReader;

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
                case 10 -> exerciseTen();
                case 11 -> exerciseEleven();
                case 12 -> exerciseTwelve();
                case 13 -> exerciseThirteen();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }


    private void exerciseThirteen() throws IOException {
        System.out.println("Enter town name:");
        String townName = bufferedReader.readLine();

        //Създаваме обект с търсения град от инпута
        Town town = entityManager
                .createQuery("SELECT t FROM Town t where t.name = :t_name", Town.class)
                .setParameter("t_name", townName)
                .getSingleResult();

        //Създаваме списък с всички адреси, които са от града от инпута
        List<Address> addresses = entityManager.createQuery("SELECT a from Address a WHERE a.town.id = :t_id", Address.class)
                .setParameter("t_id", town.getId())
                .getResultList();



        entityManager.getTransaction().begin();
        //Зануляваме TOWN_ID на всеки от адресите към града от входните данни
        //Защото таблиците са релационно свързани
        addresses.forEach(address -> address.setTown(null));
        //Премахваме адресът
        addresses.forEach(entityManager::remove);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        //След като са премахнати всички адреси, вече можем
        //да изтрием и самия град
        entityManager.remove(town);
        entityManager.getTransaction().commit();

       //В зависимост дали премахнатите адреси са един или повече, ползваме различен стринг
        String adr = addresses.size() == 1 ? "address" : "addresses";

        System.out.printf("%d %s in %s deleted", addresses.size(), adr, townName);


    }


    @SuppressWarnings("unchecked")
    private void exerciseTwelve() {
        List<Object[]> rows = entityManager.createNativeQuery("select \n" +
                "distinct\n" +
                "d.name,\n" +
                "MAX(e.salary)\n" +
                "from employees e \n" +
                "inner join departments d on d.department_id = e.department_id\n" +
                "group by d.name\n" +
                "having MAX(e.salary) not between 30000 and 70000")
                .getResultList();

        rows.forEach(objects ->
                System.out.println(objects[0] + " " + objects[1]));
    }


    private void exerciseEleven() throws IOException {
        System.out.println("Please enter first name pattern:");
        String n_like = bufferedReader.readLine() + "%";
        List<Employee> employeeList = entityManager.createQuery("select e FROM Employee e where e.firstName like :n_like", Employee.class)
                .setParameter("n_like", n_like)
                .getResultList();
        employeeList.forEach(employee ->
                System.out.printf("%s %s - %s - (%.2f)%n", employee.getFirstName(), employee.getLastName(), employee.getDepartment().getName(), employee.getSalary()));
    }

    private void exerciseTen() {
        entityManager.getTransaction().begin();
        entityManager.createQuery("UPDATE Employee e SET e.salary = e.salary * 1.12 WHERE e.department.id in (1, 2, 4, 11)")
                .executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.createQuery("SELECT e FROM Employee e WHERE e.department.id in (1, 2, 4, 11)", Employee.class)
                        .getResultList().forEach(employee -> System.out.printf("%s %s (%.2f)%n", employee.getFirstName(), employee.getLastName(), employee.getSalary()));
    }


    private void exerciseNine() {
        entityManager.createQuery("SELECT p FROM Project p ORDER BY p.startDate DESC", Project.class)
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
                .forEach(project -> System.out.println("\t" + project.getName()));
    }


    private void exerciseSeven() {
        List<Address> addresses = entityManager
                .createQuery("SELECT a FROM Address a ORDER BY a.employees.size DESC", Address.class)
                .setMaxResults(10)
                .getResultList();
        addresses.forEach(address -> System.out.printf("%s, %s - %d employees%n",
                address.getText(),
                address.getTown() == null
                        ? "Unknown" : address.getTown().getName(),
                address.getEmployees().size()));
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

        employeeList.forEach(employee -> System.out.printf("%s %s from %s - $%.2f%n",
                employee.getFirstName(),
                employee.getLastName(),
                //Обекта Емплои в Джава има достъп и до останалата информация
                //от навързаните ентитита (в случая департмент)
                employee.getDepartment().getName(),
                employee.getSalary()));
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