import { Link } from '@mono-repo-v2/web-component-wrapper';

const mockCareTeam = [
  { id: 'ct-001', name: 'Dr. Sarah Johnson', role: 'Primary Care Physician' },
  { id: 'ct-002', name: 'Dr. Michael Chen', role: 'Cardiologist' },
  { id: 'ct-003', name: 'Lisa Thompson', role: 'Care Coordinator' },
];

export function CareTeamList() {
  return (
    <div className="care-team-list">
      <h3>Your Care Team</h3>
      <ul className="member-list">
        {mockCareTeam.map((member) => (
          <li key={member.id} className="member-item">
            <Link to={`/member/${member.id}`} className="member-link">
              <span className="member-name">{member.name}</span>
              <span className="member-role">{member.role}</span>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
