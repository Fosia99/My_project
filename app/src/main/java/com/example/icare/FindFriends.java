package com.example.icare;

public class FindFriends {


        private  String profileimage,fullname, profilestatus;

        public FindFriends() {
        }

        public FindFriends(String profileimage, String fullname, String profilestatus) {
            this.profileimage = profileimage;
            this.fullname = fullname;
            this.profilestatus = profilestatus;
        }

        public String getProfileimage() {
            return profileimage;
        }

        public String getFullname() {
            return fullname;
        }

        public String getProfilestatus() {
            return profilestatus;
        }

        public void setProfileimage(String profileimage) {
            this.profileimage = profileimage;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public void setProfilestatus(String profilestatus) {
            this.profilestatus = profilestatus;
        }
    }




