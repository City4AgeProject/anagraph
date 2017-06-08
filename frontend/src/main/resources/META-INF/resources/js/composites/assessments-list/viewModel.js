define([ 'knockout', 'jquery', 'knockout-postbox', 'urls', 'entities' ],

		function(ko, $) {

			function model(context) {

				var self = this;

				self.selectedAnotations = ko.observableArray();

				self.assessmentId = ko.observable();

				self.attached = function(context) {
					ko.postbox.subscribe("refreshSelectedAssessments",
							function(selectedAssessments) {
								self.selectedAnotations(selectedAssessments);
							});
				};

				showOnDiagram = function(data, event) {
					self.assessmentId(data.id);
					ko.postbox.publish("selectDatapointsDiagram", self
							.assessmentId());
				}

				self.readMore = function() {
					document.getElementById('commentSpan').textContent = this.comment;

					$('#commentPopup').ojDialog();
					$('#commentPopup').ojDialog('open');

					// position dialog and screen
					$("#commentPopup").ojDialog('widget').css(
							'top',
							String(document.body.scrollTop + screen.height / 8)
									+ 'px');
					$("#commentPopup").ojDialog('widget').css(
							'left',
							String((docWidth - $("#commentPopup").width()) / 2)
									+ 'px');
					window.scrollTo();
				}
			}

			return model;
		});