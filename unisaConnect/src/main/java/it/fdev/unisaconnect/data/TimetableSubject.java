package it.fdev.unisaconnect.data;

public class TimetableSubject {

	private String name;
	private String color;

	public TimetableSubject(String name, String color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public static class Lesson implements Comparable<Lesson>{
		private int id;
		private String subjectName;
		private int day;
		private int startHour;
		private int startMinutes;
		private int endHour;
		private int endMinutes;
		private int duration;
		private String room;
		private int color;

		/**
		 * 
		 * @param id
		 * @param subjectName
		 * @param day
		 * @param startHour
		 * @param startMinutes
		 * @param endHour
		 * @param endMinutes
		 * @param room
		 * @param color
		 * 
		 */
		public Lesson(int id, String subjectName, int day, int startHour, int startMinutes, int endHour, int endMinutes, String room, int color) {
			this.id = id;
			this.subjectName = subjectName;
			this.day = day;
			this.startHour = startHour;
			this.startMinutes = startMinutes;
			this.endHour = endHour;
			this.endMinutes = endMinutes;
			this.room = room;
			this.color = color;
			
			this.duration = ((endHour - startHour) * 60) + (endMinutes - startMinutes);
			if (this.duration < 0) {
				this.duration = 0;
			}
		}

		public Lesson(String subjectName, int day, int startHour, int startMinutes, int endHour, int endMinutes, String room, int color) {
			this(-1, subjectName, day, startHour, startMinutes, endHour, endMinutes, room, color);
		}

		public int getId() {
			return id;
		}

		public String getSubjectName() {
			return subjectName;
		}

		public int getDay() {
			return day;
		}

		public int getStartHour() {
			return startHour;
		}

		public int getStartMinutes() {
			return startMinutes;
		}

		public int getEndHour() {
			return endHour;
		}

		public int getEndMinutes() {
			return endMinutes;
		}

		public String getRoom() {
			return room;
		}

		public int getColor() {
			return color;
		}
		
		public int getDuration() {
			return duration;
		}

		@Override
		public int compareTo(Lesson comp) {
			if(day < comp.day)
				return -1;
			if(day > comp.day)
				return 1;
			if(day == comp.day) {
				if (startHour < comp.startHour)
					return -1;
				if (startHour > comp.startHour)
					return 1;
			}
			return 0;
		}
	}
}
