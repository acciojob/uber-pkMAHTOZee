package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.model.TripStatus;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.*;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Optional<Customer> customerOptional = customerRepository2.findById(customerId);
		if(customerOptional.isPresent()){
			Customer customer = customerOptional.get();
			customerRepository2.delete(customer);
		}

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		TripBooking tripBooking = new TripBooking(fromLocation, toLocation, distanceInKm);

		Driver driver = null;
		List<Driver> driverOptional= driverRepository2.findAll();

		for (Driver d:driverOptional) {
			if (d.getCab().getAvailable() == Boolean.TRUE) {
				if (driver == null || d.getDriverId() < driver.getDriverId()) {
					driver = d;
				}
			}
		}
		if(driver==null){
			throw new Exception("No cab available!");
		}
		Customer customer = customerRepository2.findById(customerId).get();
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);
		driver.getCab().setAvailable(Boolean.FALSE);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);

		customer.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);//saving in parent

		driver.getTripBookingList().add(tripBooking);
		driverRepository2.save(driver);

		//tripBookingRepository2.save(tripBooking);//qki ye child hai to jarurat nhi hai
		return  tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBooking = tripBookingRepository2.findById(tripId);
		if(tripBooking.isPresent()){
			TripBooking tripBooking1 = tripBooking.get();
			tripBooking1.setStatus(TripStatus.CANCELED);
			tripBooking1.setBill(0);
			tripBooking1.getDriver().getCab().setAvailable(Boolean.TRUE);
			tripBookingRepository2.save(tripBooking1);
		}

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBooking = tripBookingRepository2.findById(tripId);
		if(tripBooking.isPresent()){
			TripBooking tripBooking1 = tripBooking.get();
			tripBooking1.setStatus(TripStatus.COMPLETED);
			tripBooking1.getDriver().getCab().setAvailable(Boolean.TRUE);
			int bill = tripBooking1.getDriver().getCab().getPerKmRate() * tripBooking1.getDistanceInKm();
			tripBooking1.setBill(bill);
			tripBookingRepository2.save(tripBooking1);
		}
	}
}