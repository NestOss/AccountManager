'use strict';

App.controller('OrganizationController', ['$scope', 'OrganizationService',
    function ($scope, OrganizationService) {
        var self = this;
        self.organization = {name: ''}
        self.currentOrganization = {name: null};
        self.organizations = [];
        self.curOrgToDeleteName = '';

        self.fetchAllOrganizations = function () {
            OrganizationService.fetchAllOrganizations()
                    .then(
                            function (d) {
                                self.organizations = d;
                            },
                            function (errResponse) {
                                console.error('Error while fetching Organizations');
                                showAlert(errResponse);
                            }
                    );
        };

        self.fetchAllOrganizations();

        self.createOrganization = function (organization) {
            OrganizationService.createOrganization(organization)
                    .then(
                            self.fetchAllOrganizations,
                            function (errResponse) {
                                console.error('Error while creating Organization.');
                                showAlert(errResponse);
                            }
                    );
        };

        self.updateOrganization = function (organization) {
            OrganizationService.updateOrganization(self.currentOrganization, organization)
                    .then(
                            self.fetchAllOrganizations,
                            function (errResponse) {
                                console.error('Error while updating Organization.');
                                showAlert(errResponse);
                            }
                    );
        };

        self.deleteOrganization = function (organization) {
            OrganizationService.deleteOrganization(organization)
                    .then(
                            self.fetchAllOrganizations,
                            function (errResponse) {
                                console.error('Error while deleting Organization.');
                                showAlert(errResponse);
                            }
                    );
        };

        self.submit = function () {
            if (self.currentOrganization.name === null) {
                console.log('Saving New Organization', self.organization);
                self.createOrganization(self.organization);
            } else {
                self.updateOrganization(self.organization);
                console.log('Organization updated with name ', self.currentOrganization.name);
                console.log('Organization updated to name ', self.organization.name);
            }
            self.reset();
        };

        self.edit = function (orgName) {
            console.log('Organization name to be edited', orgName);
            self.currentOrganization.name = orgName;
            self.organization.name = orgName;
            //$scope.myForm.$setDirty();
        };

        self.showConfirmDialogThenDelete = function (orgName) {
            self.curOrgToDeleteName = orgName;
            $('#dialog-confirm').dialog('open');
        }

        self.removeCurOrg = function () {
            console.log('org to be deleted', self.curOrgToDeleteName);
            // if org is updated now
            if (self.currentOrganization.name === self.curOrgToDeleteName) {
                self.reset();
            }
            self.deleteOrganization({name: self.curOrgToDeleteName});
        };

        self.reset = function () {
            self.organization = {name: ''};
            self.currentOrganization = {name: null};
            $scope.myForm.$setPristine(); //reset Form
        };

    }]);