package ase.model;

import ase.exceptions.InvalidNameException;

public class Name {
    String firstName;
    String lastName;

    /*
     * Checks that there are two valid names entered and stores them and first and last name.
     *
     */
    public Name(String fullname) throws InvalidNameException{
        String[] names = fullname.split(" ");
        String error;

        if (names.length == 0){
            error = "no names have been entered";
            throw new InvalidNameException(error);
        }
        else if (names.length < 2) {
            error = "only one name seems to have been entered.";
            throw new InvalidNameException(error);
        }
        else if (names.length>2) {
            error = "more than two names seem to have been entered.";
            throw new InvalidNameException(error);
        }else {

            if (!fullname.matches("[a-zA-Z\\s'-]+")) {
                error = "names can only contain alphabetic characters, spaces, hyphens and apostrophes.";
                throw new InvalidNameException(error);
            }

            firstName = names[0];
            lastName = names[1];
        }
    }

    public String getFullName() {
        return ("firstName " + "lastName");
    }

    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    @Override
    public String toString() {
        return this.lastName;
    }
}
