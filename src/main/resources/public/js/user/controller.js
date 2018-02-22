'use strict';

App.controller('UserController', ['$scope', 'UserService',
    function ($scope, UserService) {
        var self = this;
        self.user = {login: '',
            name: '',
            lastname: '',
            orgname: '',
            role: null,
            password: '',
            confirm: '',
            active: false,
            id: null};
        self.users = [];
        self.curUserId = '';
        self.orderProp = 'login';

        self.showForm = false;

        self.currentPage = 0;
        self.pageSize = 10;
        self.pageCount = 0;

        self.pagePanel = {
            prev: false,
            next: true,
            prevLabel: false,
            nextLabel: true,
            prevN: false,
            nextN: true,
            first: false,
            last: true
        }

        self.showHide = function () {
            self.showForm = !self.showForm;
        }

        self.showHideClass = function () {
            return self.showForm ? "lead ui-icon ui-icon-triangle-1-s" : "lead ui-icon ui-icon-triangle-1-e";
        }

        self.defPagePanelAttribute = function (num) {
            self.pagePanel = {
                prev: false,
                next: true,
                prevLabel: false,
                nextLabel: true,
                prevN: false,
                nextN: true,
                first: false,
                last: true
            };
            if (num == 1) {
                self.pagePanel.prev = false;
                self.pagePanel.first = true;
            }
            if (num > 1) {
                self.pagePanel.prev = true;
                self.pagePanel.first = true;
            }
            if (num > 2) {
                self.pagePanel.prevLabel = true;
            }
            if (num >= 5) {
                self.pagePanel.prevN = true;
            }
            var size = Math.ceil(self.users.length / self.pageSize);
            if ((num + 5) >= size) {
                self.pagePanel.nextN = false;
            }
            if ((num + 3) >= size) {
                self.pagePanel.nextLabel = false;
            }
            if (num >= (size - 2)) {
                self.pagePanel.next = false;
            }
            if (num == (size - 1)) {
                self.pagePanel.last = false;
            }
        };

        self.firstPage = function () {
            self.currentPage = 0;
            self.defPagePanelAttribute(self.currentPage);
        }

        self.lastPage = function () {
            self.currentPage = Math.ceil(self.users.length / self.pageSize) - 1;
            self.defPagePanelAttribute(self.currentPage);
        }

        self.nextPage = function (count) {
            if ((self.users.length - 1) >= (self.currentPage + count)) {
                self.currentPage += count;
            }
            self.defPagePanelAttribute(self.currentPage);
        };

        self.prevPage = function (count) {
            if (self.currentPage >= count) {
                self.currentPage -= count;
            }
            self.defPagePanelAttribute(self.currentPage);
        };

        self.getNumberAsArray = function (num) {
            return new Array(num);
        };

        self.numberOfPages = function () {
            self.pageCount = Math.ceil(self.users.length / self.pageSize);
            self.defPagePanelAttribute(self.currentPage);
            return self.pageCount;
        };

        self.convert = function (user) {
            var converted = {};
            converted.login = user.login;
            converted.password = user.password;
            converted.shortName = user.name;
            converted.commonName = user.name + " " + user.lastname;
            converted.role = user.role;
            converted.isActive = user.active;
            converted.organizationName = user.orgname;
            return converted;
        };

        self.fetchAllUsers = function () {
            UserService.fetchAllUsers()
                    .then(
                            function (d) {
                                self.users = d;
                            },
                            function (errResponse) {
                                console.error('Error while fetching Organizations');
                                showAlert(errResponse);
                            }
                    );
        };

        self.fetchAllUsers();

        self.createUser = function (user) {
            UserService.createUser(self.convert(user))
                    .then(
                            self.fetchAllUsers,
                            function (errResponse) {
                                console.error('Error while creating User.');
                                showAlert(errResponse);
                            }
                    );
        };

        self.updateUser = function (user) {
            UserService.updateUser(self.convert(user))
                    .then(
                            self.fetchAllUsers,
                            function (errResponse) {
                                console.error('Error while updating User.');
                                showAlert(errResponse);
                            }
                    );
        };

        self.deleteUser = function (userId) {
            if (self.users.length % self.pageSize == 1) {
                self.prevPage(1);
                self.defPagePanelAttribute(self.currentPage);
            }
            var usr = {};
            usr.login = userId;
            UserService.deleteUser(usr)
                    .then(
                            self.fetchAllUsers,
                            function (errResponse) {
                                console.error('Error while deleting User.');
                                showAlert(errResponse);
                            }
                    );

        };

        self.changeOrder = function (order) {
            self.orderProp = order;
        };

        self.submit = function () {
            if (self.user.id === null) {
                console.log('Saving New User', self.user);
                self.createUser(self.user);
            } else {
                self.updateUser(self.user);
                console.log('User updated with login ', self.user.id);
                console.log('User updated to parameters ', self.user);
            }
            self.reset();
        };

        self.edit = function (u) {
            self.showForm = true;
            console.log('User to be edited', u);
            self.user.id = u.login;
            self.user.login = u.login;
            self.user.orgname = u.organizationName;
            self.user.role = u.role;
            var commonName = u.commonName.split(' ');
            self.user.name = commonName[0];
            self.user.lastname = commonName[1];
            self.user.active = u.isActive;
            if ($("#icon").hasClass("ui-icon-triangle-1-e")) {
                $("#icon").removeClass("ui-icon-triangle-1-e");
                $("#icon").addClass("ui-icon-triangle-1-s");
                $("#editform").show(100);
            }
            $scope.myForm.$setDirty();
        };

        self.showConfirmDialogThenDelete = function (userId) {
            self.curUserId = userId;
            $('#dialog-confirm').dialog('open');
        };

        self.showChangePasswordDialog = function (userId) {
            self.curUserId = userId;
            console.log('invopcation of the dialod show for ', self.curUserId);
            $("#changedLogin").val(userId);
            $('#dialog-change').dialog('open');
        };

        self.removeCurUser = function () {
            console.log('user to be deleted', self.curUserId);
            if (self.user.id === self.curUserId) {//clean form if the organization to be deleted is shown there.
                self.reset();
            }
            self.deleteUser(self.curUserId);
        };

        self.changePassword = function (pass) {
            console.log('change password of the user with login ', self.curUserId);
            var usr = {};
            usr.login = self.curUserId;
            usr.password = pass;
            UserService.changeUserPassword(usr)
                    .then(
                            self.fetchAllUsers,
                            function (errResponse) {
                                console.error('Error while changing User password.');
                                showAlert(errResponse);
                            }
                    );
            console.log('change password to ', pass);
        };

        self.reset = function () {
            self.user = {login: '',
                name: '',
                lastname: '',
                orgname: '',
                role: null,
                password: '',
                confirm: '',
                active: false,
                id: null};
            $scope.myForm.$setPristine(); //reset Form
        };

    }]);

var compareTo = function () {
    return {
        require: "ngModel",
        scope: {
            otherModelValue: "=compareTo"
        },
        link: function (scope, element, attributes, ngModel) {
            ngModel.$validators.compareTo = function (modelValue) {
                return modelValue == scope.otherModelValue;
            };

            scope.$watch("otherModelValue", function () {
                ngModel.$validate();
            });
        }
    };
};

App.directive("compareTo", compareTo);
App.filter('startFrom', function () {
    return function (input, start) {
        return input.slice(start);
    };
});