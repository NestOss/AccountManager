'use strict';

App.factory('GroupAdminsService', ['$http', '$q', '$document', function ($http, $q, $document) {
        self.csrfHeaderName = $document[0].querySelector("meta[name='_csrf_header']").getAttribute('content');
        self.csrf = $document[0].querySelector("meta[name='_csrf']").getAttribute('content');
        self.headers = {};
        self.headers[self.csrfHeaderName] = self.csrf;
        self.headers["Content-Type"] = 'application/json';
        self.domain = $document[0].querySelector("meta[name='_domain']").getAttribute('content');
        return {
            fetchAllUsers: function () {
                return $http.get('/accountmanager/group_administrator'+ self.domain)
                        .then(
                                function (response) {
                                    return response.data;
                                },
                                function (errResponse) {
                                    console.error('Error while fetching organizations');
                                    return $q.reject(errResponse);
                                }
                        );
            },createUser: function (user) {
                return $http.post('/accountmanager/group_administrator'+self.domain,
                        JSON.stringify(user), {headers: self.headers})
                        .then(
                                function (response) {
                                    return response.data;
                                },
                                function (errResponse) {
                                    console.error('Error while creating user');
                                    return $q.reject(errResponse);
                                }
                        );
            },deleteUser: function (user) {
                return $http({method: 'DELETE',
                    url: '/accountmanager/group_administrator/item/',
                    data: JSON.stringify(user),
                    headers: self.headers
                }).then(
                                function (response) {
                                    return response.data;
                                },
                                function (errResponse) {
                                    console.error('Error while deleting user');
                                    return $q.reject(errResponse);
                                }
                        );
            },updateUser: function (user) {
                return $http.put('/accountmanager/group_administrator'+ self.domain,
                        JSON.stringify(user), {headers: self.headers})
                        .then(
                                function (response) {
                                    return response.data;
                                },
                                function (errResponse) {
                                    console.error('Error while updating user');
                                    return $q.reject(errResponse);
                                }
                        );
            },changeUserPassword: function (user) {
                return $http.put('/accountmanager/group_administrator/pass',
                        JSON.stringify(user), {headers: self.headers})
                        .then(
                                function (response) {
                                    return response.data;
                                },
                                function (errResponse) {
                                    console.error('Error while updating user');
                                    return $q.reject(errResponse);
                                }
                        );
            }
        };

    }]);