package com.serene.sems.config;

import com.serene.sems.model.Customer;
import com.serene.sems.model.Dealer;
import com.serene.sems.model.Role;
import com.serene.sems.model.User;
import com.serene.sems.repository.CustomerRepository;
import com.serene.sems.repository.DealerRepository;
import com.serene.sems.repository.OrderRepository;
import com.serene.sems.repository.RoleRepository;
import com.serene.sems.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Seeds Indian demo dealers/customers. When the marker user is absent, purges all bulk-demo username
 * prefixes (v1–v3) so dealer emails are free, then inserts {@code bharat3_dlr_*} with fixed full names from
 * classpath resources.
 */
@Service
public class IndianDemoBulkSeed {

    private static final Logger log = LoggerFactory.getLogger(IndianDemoBulkSeed.class);

    /** Current demo generation — bump when demo rows must be replaced on all installs. */
    private static final String INDIAN_MARKER_USER = "bharat3_dlr_01";

    /**
     * All usernames sharing these prefixes are removed (with dealers/orders/customers) before a fresh seed.
     */
    private static final List<String> DEMO_USER_PREFIXES_TO_PURGE =
            List.of("bharat3_dlr_", "bharat2_dlr_", "bharat_dlr_", "serene_dealer_");

    /** Seeded dealer emails use this domain so they never clash with prior demo domains. */
    private static final String DEMO_EMAIL_DOMAIN_SUFFIX = "@v3.bharatsems.demo";

    private static final int DEMO_DEALER_COUNT = 25;
    private static final int CUSTOMERS_PER_DEALER = 40;

    /** Must appear before {@link #DEMO_CUSTOMER_FULL_NAMES} — static fields init in source order. */
    private static final String[] DEMO_NAME_RESOURCE_PARTS = {
        "demo-customer-names-part1.txt",
        "demo-customer-names-part2.txt",
        "demo-customer-names-part3.txt",
        "demo-customer-names-part4.txt"
    };

    private static final List<String> DEMO_CUSTOMER_FULL_NAMES = loadDemoCustomerFullNames();

    private static final List<DemoOutlet> DEMO_OUTLETS = List.of(
            new DemoOutlet("Sharma & Sons Traders", "Mumbai", "MH"),
            new DemoOutlet("Gupta Enterprises", "Pune", "MH"),
            new DemoOutlet("Patel Distributors", "Nagpur", "MH"),
            new DemoOutlet("Joshi Sales Hub", "Nashik", "MH"),
            new DemoOutlet("Kulkarni Agencies", "Thane", "MH"),
            new DemoOutlet("Mehta Mart", "Ahmedabad", "GJ"),
            new DemoOutlet("Shah Commercials", "Surat", "GJ"),
            new DemoOutlet("Desai & Company", "Vadodara", "GJ"),
            new DemoOutlet("Parekh Brothers", "Rajkot", "GJ"),
            new DemoOutlet("Reddy Associates", "Hyderabad", "TG"),
            new DemoOutlet("Rao Trading Co.", "Bengaluru", "KA"),
            new DemoOutlet("Iyer & Sons", "Chennai", "TN"),
            new DemoOutlet("Natarajan Stores", "Coimbatore", "TN"),
            new DemoOutlet("Murugan Enterprises", "Madurai", "TN"),
            new DemoOutlet("Bose Merchants", "Kolkata", "WB"),
            new DemoOutlet("Kapoor Retail", "New Delhi", "DL"),
            new DemoOutlet("Malhotra Distributors", "Gurgaon", "HR"),
            new DemoOutlet("Agarwal Traders", "Noida", "UP"),
            new DemoOutlet("Tiwari Sales", "Lucknow", "UP"),
            new DemoOutlet("Yadav Enterprises", "Kanpur", "UP"),
            new DemoOutlet("Mishra & Co.", "Varanasi", "UP"),
            new DemoOutlet("Verma Mart", "Indore", "MP"),
            new DemoOutlet("Shrivastava Agencies", "Bhopal", "MP"),
            new DemoOutlet("Singh Brothers", "Jaipur", "RJ"),
            new DemoOutlet("Kumar Trading", "Patna", "BR"));

    private static final String[] AREAS = {
        "MG Road", "Sector 18", "Lajpat Nagar", "Park Street", "FC Road", "Anna Salai", "Hazratganj",
        "Civil Lines", "Rajwada", "Banjara Hills", "Koramangala", "Salt Lake", "Connaught Place",
        "Sector 62", "Gomti Nagar", "C-Scheme", "Vijay Nagar", "Boring Road", "Johari Bazaar"
    };

    private record DemoOutlet(String companyName, String city, String stateCode) {}

    private static List<String> loadDemoCustomerFullNames() {
        ClassLoader cl = IndianDemoBulkSeed.class.getClassLoader();
        List<String> names = new ArrayList<>(DEMO_DEALER_COUNT * CUSTOMERS_PER_DEALER);
        for (String resource : DEMO_NAME_RESOURCE_PARTS) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(cl.getResourceAsStream(resource), "Missing resource: " + resource),
                    StandardCharsets.UTF_8))) {
                reader.lines()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(names::add);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read " + resource, e);
            }
        }
        int expected = DEMO_DEALER_COUNT * CUSTOMERS_PER_DEALER;
        if (names.size() != expected) {
            throw new IllegalStateException(
                    "Expected " + expected + " demo customer names from resources, got " + names.size());
        }
        return List.copyOf(names);
    }

    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final RoleRepository roleRepository;

    public IndianDemoBulkSeed(
            UserRepository userRepository,
            DealerRepository dealerRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.dealerRepository = dealerRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void ensureIndianDemoSeeded(PasswordEncoder passwordEncoder) {
        if (userRepository.existsByUsername(INDIAN_MARKER_USER)) {
            log.debug("Indian demo v3 already present ({}), skip", INDIAN_MARKER_USER);
            return;
        }

        purgeBulkDemoBeforeReseed();

        if (DEMO_OUTLETS.size() != DEMO_DEALER_COUNT) {
            throw new IllegalStateException("DEMO_OUTLETS size must match DEMO_DEALER_COUNT");
        }

        Role dealerRole = roleRepository.findByName("DEALER").orElseThrow();
        List<Dealer> dealers = new ArrayList<>(DEMO_DEALER_COUNT);

        for (int i = 1; i <= DEMO_DEALER_COUNT; i++) {
            DemoOutlet outlet = DEMO_OUTLETS.get(i - 1);
            String username = String.format("bharat3_dlr_%02d", i);
            User user = new User();
            user.setUsername(username);
            user.setEmail(String.format("dealer%02d%s", i, DEMO_EMAIL_DOMAIN_SUFFIX));
            user.setPassword(passwordEncoder.encode("Dealer123!"));
            user.setEnabled(true);
            user.getRoles().add(dealerRole);
            user = userRepository.save(user);

            Dealer dealer = new Dealer();
            dealer.setUser(user);
            dealer.setCompanyName(outlet.companyName());
            dealer.setPhone(String.format("+91 98765 %05d", i * 1000 + 100));
            dealer.setAddress((12 + i) + ", " + AREAS[i % AREAS.length] + ", " + outlet.city());
            dealer.setCountryCode("IN");
            dealer.setStateCode(outlet.stateCode());
            dealer.setCity(outlet.city());
            dealer.setActive(true);
            dealers.add(dealerRepository.save(dealer));
        }

        log.info(
                "Seeded {} Indian demo dealers (login {} .. bharat3_dlr_25 / Dealer123!)",
                DEMO_DEALER_COUNT,
                INDIAN_MARKER_USER);

        final int batchSize = 250;
        List<Customer> batch = new ArrayList<>(batchSize);
        int customerTotal = 0;

        for (int d = 0; d < dealers.size(); d++) {
            Dealer dealer = dealers.get(d);
            DemoOutlet outlet = DEMO_OUTLETS.get(d);
            int dealerNo = d + 1;
            for (int c = 1; c <= CUSTOMERS_PER_DEALER; c++) {
                Customer customer = new Customer();
                customer.setDealer(dealer);
                int globalIdx = (dealerNo - 1) * CUSTOMERS_PER_DEALER + (c - 1);
                customer.setFullName(DEMO_CUSTOMER_FULL_NAMES.get(globalIdx));
                customer.setPhone(uniqueDemoPhone(dealerNo, c));
                customer.setAddress(
                        (c % 150 + 1) + ", " + AREAS[(dealerNo + c) % AREAS.length] + ", " + outlet.city());
                customer.setCountryCode("IN");
                customer.setStateCode(outlet.stateCode());
                customer.setCity(outlet.city());
                customer.setActive(true);
                batch.add(customer);
                customerTotal++;
                if (batch.size() >= batchSize) {
                    customerRepository.saveAll(batch);
                    batch.clear();
                }
            }
        }
        if (!batch.isEmpty()) {
            customerRepository.saveAll(batch);
        }

        log.info("Seeded {} demo customers ({} per dealer; fixed name list)", customerTotal, CUSTOMERS_PER_DEALER);
    }

    private static String uniqueDemoPhone(int dealerNo, int customerIndex1Based) {
        long n = (long) dealerNo * 1_000_000L + customerIndex1Based;
        int tail = (int) (n % 100_000_000L);
        return String.format("+91 98%08d", tail);
    }

    /**
     * Removes bulk-demo dealers: by username prefix, by legacy seed email pattern, and by v2/v3 email
     * domains (partial runs). Resolves dealers via {@link DealerRepository#findByUser(User)} as well as
     * username so orphan {@code dealer01@bharatsems.demo} rows with non-standard usernames are removed.
     */
    private void purgeBulkDemoBeforeReseed() {
        Map<Long, User> usersById = new LinkedHashMap<>();
        for (String prefix : DEMO_USER_PREFIXES_TO_PURGE) {
            for (User u : userRepository.findByUsernameStartingWith(prefix)) {
                usersById.putIfAbsent(u.getId(), u);
            }
        }
        /*
         * Spring Data "StartingWith" / SQL LIKE treats '_' as a wildcard, so prefix "bharat3_dlr_" does not
         * reliably match usernames like bharat3_dlr_02. Collect exact numbered demo logins explicitly.
         */
        collectNumberedDealerDemoUsers(usersById, "bharat3_dlr_", DEMO_DEALER_COUNT);
        collectNumberedDealerDemoUsers(usersById, "bharat2_dlr_", 40);
        collectNumberedDealerDemoUsers(usersById, "bharat_dlr_", 40);
        for (int i = 1; i <= 50; i++) {
            userRepository
                    .findByUsername(String.format(Locale.ROOT, "serene_dealer_%02d", i))
                    .ifPresent(u -> usersById.putIfAbsent(u.getId(), u));
        }
        for (User u : userRepository.findByEmailLike("dealer%@bharatsems.demo")) {
            usersById.putIfAbsent(u.getId(), u);
        }
        for (User u : userRepository.findByEmailEndingWith(DEMO_EMAIL_DOMAIN_SUFFIX)) {
            usersById.putIfAbsent(u.getId(), u);
        }
        for (User u : userRepository.findByEmailEndingWith("@v2.bharatsems.demo")) {
            usersById.putIfAbsent(u.getId(), u);
        }
        if (usersById.isEmpty()) {
            return;
        }

        Set<Long> dealerIds = new LinkedHashSet<>();
        for (User u : usersById.values()) {
            dealerRepository.findByUserUsername(u.getUsername()).ifPresent(d -> dealerIds.add(d.getId()));
            dealerRepository.findByUser(u).ifPresent(d -> dealerIds.add(d.getId()));
        }

        if (!dealerIds.isEmpty()) {
            orderRepository.deleteAll(orderRepository.findByDealerIdIn(dealerIds));
            customerRepository.deleteAll(customerRepository.findByDealerIdIn(dealerIds));
            dealerRepository.deleteAllById(dealerIds);
        }
        userRepository.deleteAll(usersById.values());
        userRepository.flush();
        log.info(
                "Removed {} bulk-demo user(s) (demo username prefixes + seed email patterns) and linked orders/customers/dealers",
                usersById.size());
    }

    private void collectNumberedDealerDemoUsers(Map<Long, User> usersById, String basePrefix, int maxIndex) {
        for (int i = 1; i <= maxIndex; i++) {
            String username = String.format(Locale.ROOT, "%s%02d", basePrefix, i);
            userRepository.findByUsername(username).ifPresent(u -> usersById.putIfAbsent(u.getId(), u));
        }
    }
}
